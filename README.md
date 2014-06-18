SSAST Minecraft Launcher
=============

Github推荐我弄个README，我就弄了。

这个东西是一个Minecraft的启动器，就是更新游戏和启动Minecraft的程序。功能和正版启动器差不多，不过可以支持一些第三方登录，比如SkinMe、MineLogin（已经跪了貌似？）和最开始做这个的目的：SSAST服务器。添加第三方登录也很容易，有意愿可以联系我：herbix@163.com。

这是个Eclipse项目，下载，解压，导入到Eclipse中，然后就可以使用了。

如果只是想用的话build里有编译好的。

另：Mojang公司是有中止我开发这个启动器的权利的，从这里fork出去的版本也是一样。

最新版本
=============
<b>1.6.6</b><br>
会给离线玩家也设置uuid，不过不和minecraft原有的uuid机制相匹配<br>
删除了本地化里面多余的语句<br>
添加了繁体中文版本<br>
修复了会出现ExceptionInInitializerError的问题<br>
加了启动时出错的提示，并发出错误报告<br>
错误报告添加了版本号<br>

未来版本
=============
<b>1.6.7</b><br>
添加了对launcher_profiles.json文件的读取和写入（仅限于正版登录方式）<br>
正版登录如果没有成功，可以从历史记录中选择<br>
修复了更新时可能会等很久的bug（貌似1.5.2版本没有好好修复……）<br>
检查更新时又用回了LastModified<br>

历史版本
=============
请参阅<a href="update-log">update-log</a>
