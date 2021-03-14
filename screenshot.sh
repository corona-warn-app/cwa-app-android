#!/bin/zsh
bundle exec fastlane screengrab
fastlane run zip output_path:"fastlane/metadata/screenshots.zip" path:"fastlane/metadata/android"
