# SGui (Server Gui)
It's a small, jij-able library that allows creation of server side guis.

**NOTE:** *This is a forge port of [Patbox's sgui fabric lib](https://github.com/Patbox/sgui).*
*If you find this library useful, make sure to give him a star!*

## Usage:
Add it to your dependencies like this:

```
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
	implementation fg.deobf 'com.github.samolego:forgified-sgui:[TAG]'
	
	// You can also shadow it as well (jij dependency)
	shadow 'com.github.samolego:forgified-sgui:[TAG]'
}
```

After that you are ready to go! You can use SimpleGUI and other classes directly for simple ones or extend
them for more complex guis.