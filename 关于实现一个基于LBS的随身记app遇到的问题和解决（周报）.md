##  实现第三方登录功能
###  注册qq开发者平台账号
1：提交审核信息的按钮是头像（吐槽一波，这谁想得到）
2：名称的意思是身份证上的姓名（吐槽第二波，常人一看就以为是昵称之类的，随便填写导致一直审核不通过）
3：填写创建应用信息时，安装包签名不知道填什么，官方提供了一个签名获取包，但是只针对于apk，因此要先生成一个apk才行
###  尝试生成一个apk
1：遇到了如下问题![在这里插入图片描述](https://img-blog.csdnimg.cn/20200306113826604.png)
查看代码发现无错，度娘说是导入的第三方安装包过大，需要加入
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200306114326253.png)
然后成功生成apk
2：发布的apk手机上安装失败，同时，我发现将官方给的apk可以通过AS直接安装到手机，并成功获得签名，但是我用AS生成的apk为什么无法安装？

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200306115816277.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0l0X2lzX0lUXw==,size_16,color_FFFFFF,t_70)
原因：v2是android7.0引入的一个签名机制，相比v1使得apk更安全及在安装时速度更快，但有可能会引起问题，具体可能会遇到什么问题并没有说，所以这个v2签名机制并不是强制性的，原文说如果不能正确构建则可以不用v2签名，只用v1签名。(以为是单选并且只选择了V2)![在这里插入图片描述](https://img-blog.csdnimg.cn/20200306120140182.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0l0X2lzX0lUXw==,size_16,color_FFFFFF,t_70)
然后慢慢等待审核
3：审核效率偏慢但还是审核通过了。嗯/
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200307184744506.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0l0X2lzX0lUXw==,size_16,color_FFFFFF,t_70)
4：成功实现了qq登录功能，有趣的是按钮的监测功能的写法都不一样了，![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308002122772.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0l0X2lzX0lUXw==,size_16,color_FFFFFF,t_70)
5：在实现qq分享功能时碰到了问题，![在这里插入图片描述](https://img-blog.csdnimg.cn/20200308002300725.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0l0X2lzX0lUXw==,size_16,color_FFFFFF,t_70)
顺便==吐槽==一下腾讯为什么要搞两个网站，一个qq互联，一个腾讯开发者平台，功能还独立相似，又能互相关联起来。经过一顿细致入微的排查，应该是我在qq互联上填写的包名有问题，没有和当前项目的包名对应。改了就好了，必须重申下：==什么两行代码，写了三俩回调函数和一个监听函数，至少三十行==![在这里插入图片描述](https://img-blog.csdnimg.cn/202003080036211.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0l0X2lzX0lUXw==,size_16,color_FFFFFF,t_70)
##  调用相册和摄像机功能
###  上周实现了摄像机的调用和照片的显示，这周尝试读取相册
1：权限问题
在动态申请手机SD卡权限时，程序总是崩溃；
已知关于安卓系统6.0以上的版本有一些危险权限需要声明，比如SD卡储存，而在6.0以下的版本则不需要，直接注册即可。但是我的手机是5.1版本，这是否意味着低版本的系统在申请权限时如果采用动态申请。会出现崩溃的现象？
经验证，借了同学的手机，发现权限是没有问题的，==动态申请权限相当于一个附加的内容，可以在任何版本上使用==，只是不执行。问题的来源在于我在写intent时写错了一个符号，最气的是AS居然不报错~~不谨慎乃万恶之源
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200307123417682.png)
2：成功打开了相册，如何将相册显示到imageview中，参考网上的解释，似乎不同的版本返回的Uri值也不一样，因此需要根据不同的UrI进行解析和使用。
这部分花费了挺多时间的，但是总算是写出来了。但是上传到服务器的话暂时先不写了
3：==剩余一个问题==，在小米Android10.0bban版本运行时无法打开摄像头？![在这里插入图片描述](https://img-blog.csdnimg.cn/20200307184933166.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0l0X2lzX0lUXw==,size_16,color_FFFFFF,t_70)



