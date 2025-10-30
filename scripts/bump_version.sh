#!/usr/bin/env bash
set -e

FILE="build.gradle.kts"
# Extract current appVersion X.Y.Z (source variable)
CURRENT=$(grep -E 'val appVersion\s*=\s*"[0-9]+\.[0-9]+\.[0-9]+"' -m1 "$FILE" | sed -E 's/.*"([0-9]+\.[0-9]+\.[0-9]+)".*/\1/')
if [ -z "$CURRENT" ]; then
  echo "Could not find current appVersion in $FILE" >&2
  exit 1
fi
# Parse version into components
MA=$(echo "$CURRENT" | cut -d. -f1)
MI=$(echo "$CURRENT" | cut -d. -f2)
PA=$(echo "$CURRENT" | cut -d. -f3)

# Determine bump type from commits since last tag
# 0 = patch, 1 = minor, 2 = major
BUMP_TYPE=0

# Get last tag, handling errors gracefully
COMMITS=""
set +e
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null)
TAG_STATUS=$?
set -e

if [ $TAG_STATUS -ne 0 ] || [ -z "$LAST_TAG" ]; then
  set +e
  COMMITS=$(git log --pretty=format:%s HEAD 2>/dev/null)
  set -e
else
  set +e
  COMMITS=$(git log --pretty=format:%s "$LAST_TAG"..HEAD 2>/dev/null)
  set -e
fi

if [ -n "$COMMITS" ]; then
  # Check all commits for breaking changes first (highest priority)
  if echo "$COMMITS" | grep -qE '![:]|BREAKING[[:space:]]*CHANGE' || echo "$COMMITS" | grep -q "BREAKING CHANGE"; then
    BUMP_TYPE=2
  # Check for feat: commits (minor version bump)
  elif echo "$COMMITS" | grep -qE '^feat[^:]*:'; then
    BUMP_TYPE=1
  fi
  # Otherwise default to patch (BUMP_TYPE=0)
fi

# Apply bump
case $BUMP_TYPE in
  2) MA=$((MA+1)); MI=0; PA=0 ;;
  1) MI=$((MI+1)); PA=0 ;;
  0) PA=$((PA+1)) ;;
esac

NEW="$MA.$MI.$PA"
case $BUMP_TYPE in
  2) BUMP_NAME="MAJOR" ;;
  1) BUMP_NAME="MINOR" ;;
  0) BUMP_NAME="PATCH" ;;
  *) BUMP_NAME="PATCH" ;;
esac

echo "Current: $CURRENT -> New: $NEW ($BUMP_NAME bump)"
echo "Analyzed commits:"
echo "$COMMITS"

# Update appVersion (source variable - this updates both version and packageVersion)
sed -i '' -E "s/(val appVersion\s*=\s*\")([0-9]+\.[0-9]+\.[0-9]+)(\".*)/\1$NEW\3/" "$FILE"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$FILE"
git commit -m "chore(release): v$NEW" || echo "No changes to commit"
git tag "v$NEW" || echo "Tag exists"
git push origin HEAD:main || true
git push origin --tags || true

echo "version=$NEW" >> $GITHUB_OUTPUT
echo "bump_type=$BUMP_NAME" >> $GITHUB_OUTPUT

