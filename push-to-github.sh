#!/bin/bash

# This script helps push the project to GitHub

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Premium Document Converter GitHub Upload Script${NC}"
echo "This script will help you push your project to GitHub."
echo ""

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo -e "${RED}Git is not installed. Please install Git first.${NC}"
    exit 1
fi

# Check if gh is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}GitHub CLI is not installed. Please install GitHub CLI first.${NC}"
    exit 1
fi

# Prompt for GitHub username
echo -e "${YELLOW}Please enter your GitHub username:${NC}"
read github_username

# Prompt for repository name
echo -e "${YELLOW}Please enter a name for your repository (default: premium-doc-converter):${NC}"
read repo_name
repo_name=${repo_name:-premium-doc-converter}

# Check if already logged in to GitHub
echo -e "${YELLOW}Checking GitHub login status...${NC}"
gh auth status &> /dev/null
if [ $? -ne 0 ]; then
    echo -e "${YELLOW}You need to log in to GitHub. Running 'gh auth login'...${NC}"
    gh auth login
else
    echo -e "${GREEN}Already logged in to GitHub.${NC}"
fi

# Create the repository if it doesn't exist
echo -e "${YELLOW}Creating GitHub repository (if it doesn't already exist)...${NC}"
gh repo create $repo_name --public --description "Premium Document Conversion App with Format Preservation" --confirm || true

# Initialize git if needed
if [ ! -d .git ]; then
    echo -e "${YELLOW}Initializing git repository...${NC}"
    git init
    git branch -M main
fi

# Add files to git
echo -e "${YELLOW}Adding files to git...${NC}"
git add .

# Commit changes
echo -e "${YELLOW}Committing changes...${NC}"
git commit -m "Initial commit of Premium Document Converter app"

# Set the remote origin
echo -e "${YELLOW}Setting remote origin...${NC}"
git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/$github_username/$repo_name.git

# Push to GitHub
echo -e "${YELLOW}Pushing to GitHub...${NC}"
git push -u origin main

echo -e "${GREEN}Done! Your project has been pushed to GitHub.${NC}"
echo -e "View your repository at: ${YELLOW}https://github.com/$github_username/$repo_name${NC}"
echo -e "GitHub Actions will now build your APK automatically."
echo -e "Check the 'Actions' tab on your repository to download the APK when it's ready."
