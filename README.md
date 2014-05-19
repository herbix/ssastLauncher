SSAST Minecraft Launcher
=============

Github推荐我弄个README，我就弄了。

这个东西是一个Minecraft的启动器，就是更新游戏和启动Minecraft的程序。功能和正版启动器差不多，不过可以支持一些第三方登录，比如SkinMe、MineLogin（已经跪了貌似？）和最开始做这个的目的：SSAST服务器。添加第三方登录也很容易，有意愿可以联系我：herbix@163.com。

这是个Eclipse项目，下载，解压，导入到Eclipse中，然后就可以使用了。

如果只是想用的话build里有编译好的。

另：Mojang公司是有中止我开发这个启动器的权利的，从这里fork出去的版本也是一样。

最新版本
=============
<b>1.6.5</b><br>
重构了ServerAuth相关类名<br>
不再生成_run.jar文件<br>
运行前检测jre是32位还是64位<br>
下载时要下载32位和64位对应的native库<br>
现在native库只有在运行时才会解压<br>
增加了twitch直播功能<br>
可以进行离线直播（选择Twitch登录方式并用Mojang账号登录）<br>

未来版本
=============
<b>1.6.6</b><br>
会给离线玩家也设置uuid，不过不一定和minecraft原有的uuid机制相匹配<br>

历史版本
=============
请参阅<a href="update-log">update-log</a>
