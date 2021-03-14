if [ -d $ANDROID_SDK_ROOT ]
then
    echo "Directory $ANDROID_SDK_ROOT already exists so we're skipping the install. If you'd like to install fresh tools, edit this script to invalidate the CI cache..."
    exit 0
fi

mkdir -p $ANDROID_SDK_ROOT
cd $ANDROID_SDK_ROOT
curl https://dl.google.com/android/repository/commandlinetools-mac-6858069_latest.zip -o cmdline-tools.zip

unzip cmdline-tools.zip -d $ANDROID_SDK_ROOT/cmdline-tools
mv $ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest

mkdir -p "$ANDROID_SDK_ROOT/licenses"

echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_SDK_ROOT/licenses/android-sdk-license"
echo "84831b9409646a918e30573bab4c9c91346d8abd" > "$ANDROID_SDK_ROOT/licenses/android-sdk-preview-license"
echo "d975f751698a77b662f1254ddbeed3901e976f5a" > "$ANDROID_SDK_ROOT/licenses/intel-android-extra-license"

SDKMANAGER=$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager

$SDKMANAGER "platform-tools"
$SDKMANAGER "platforms;android-29"
$SDKMANAGER "build-tools;29.0.3"
$SDKMANAGER "ndk-bundle"
$SDKMANAGER "system-images;android-29;google_apis;x86_64"
$SDKMANAGER "emulator"

echo "y" | sudo $SDKMANAGER --install "ndk;21.2.6472646" --sdk_root=${ANDROID_SDK_ROOT}