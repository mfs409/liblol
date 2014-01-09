# get the names we need
echo -n "Enter your game name (e.g., doggame) :> "
read gamename
echo -n "Enter your package name (e.g., com.petgames.doggame) :> "
read pkgname
echo "Using game name == $gamename, package name == $pkgname"
echo -n "Press enter to continue, control-c to quit :> "
read z
pth=`echo $pkgname | sed 's/\./\//g'`

# Clean up the main code folder
cd my-lol-game/src
#  change the package name
sed -i "s/com.me.mylolgame/$pkgname/g" com/me/mylolgame/*
#  move the code into the right folder
mkdir -p $pth
git mv com/me/mylolgame/* $pth/
rmdir com/me/mylolgame
rmdir com/me
#  fix the metadata files
cd ..
sed -i "s/my-lol-game/$gamename/" .project
#  change the folder name
cd ..
mkdir -p $gamename
git mv my-lol-game/* $gamename
git mv my-lol-game/.[cps]* $gamename
rmdir my-lol-game

# clean up the Android folder
cd my-lol-game-android/src
#  change the package name
sed -i "s/com.me.mylolgame/$pkgname/g" com/me/mylolgame/*
#  move the code into the right folder
mkdir -p $pth
git mv com/me/mylolgame/* $pth/
rmdir com/me/mylolgame
rmdir com/me
#  fix the metadata files
cd ..
sed -i "s/my-lol-game/$gamename/g" .classpath
sed -i "s/my-lol-game/$gamename/g" .project
sed -i "s/com.me.mylolgame/$pkgname/g" AndroidManifest.xml
#  change the folder name
cd ..
mkdir -p $gamename-android
git mv my-lol-game-android/* $gamename-android
git mv my-lol-game-android/.[cps]* $gamename-android
rmdir my-lol-game-android

# clean up the Desktop folder
cd my-lol-game-desktop/src
#  change the package name
sed -i "s/com.me.mylolgame/$pkgname/g" com/me/mylolgame/*
#  move the code into the right folder
mkdir -p $pth
git mv com/me/mylolgame/* $pth/
rmdir com/me/mylolgame
rmdir com/me
#  fix the metadata files
cd ..
sed -i "s/my-lol-game/$gamename/g" .classpath
sed -i "s/my-lol-game/$gamename/" .project
#  change the folder name
cd ..
mkdir -p $gamename-desktop
git mv my-lol-game-desktop/* $gamename-desktop
git mv my-lol-game-desktop/.[cps]* $gamename-desktop
rmdir my-lol-game-desktop

# fix the fetch.xml script
sed -i "s/my-lol-game/$gamename/" fetch.xml

# commit changes
git add * > /dev/null
git commit -m "ran rename.sh to configure $gamename" > /dev/null

# All done
echo "Done"
