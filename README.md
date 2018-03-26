[博客介绍连接-Android WebDAV](https://www.jianshu.com/p/19beeec1aa75)

####1. 主要的功能：	
&#160; &#160; &#160; &#160;功能：文件的一些基本的操作+上传下载+WebDAV文件的播放，基本的功能我非常简单的测试了下应该是可以的。
![Android WebDAV客户端整体功能图](http://img.blog.csdn.net/20150716095442600)

####2. 注意点：
1. 如果代码编译不过去就把gradle里面的computeVersionName 注释掉随便写个versionName。
2. 为了方便代码里面用到了EventBust类库，并且在消息这一块写的不是很好。所以代码很乱很杂。
3. 在mobile 下来的res/values/config.xml里面配置WebDAV服务器的domain,root,password。如下

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="webdav_domain">192.168.31.153</string>
    <string name="webdav_root">root</string>
    <string name="webdav_password">admin</string>
</resources>
```


&#160; &#160; &#160; &#160;之前写过一个基于WebDAV协议实现文件操作的Android APP，也一直有人私下问我关于这部分的实现。借着大家提的一些问题对WebDAV APP做了一些简单的整理。大部分人都说看不到直观的效果。所以这次特意在坚果云服务器上申请了一个账号，然后用咱们写的APP来操作坚果云服务器上的文件。(账号：1007178106@qq.com 密码：jianguoyun123456)。
### 一，效果

#### 基于WebDAV协议我们实现的功能有
- 1. 文件浏览
> 坚果云服务器上的文件
![web_文件列表.png](https://upload-images.jianshu.io/upload_images/9182331-ee4afcda5ab60b66.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 验证APP上文件列表是否和坚果云服务器上的文件列表一致
![WebDAV文件列表.png](https://upload-images.jianshu.io/upload_images/9182331-5fce5a88aab8ef6c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 2. 文件创建

&#160; &#160; &#160; &#160;我们在APP上新建一个文件夹 new。然后再坚果云服务器上看新建的文件是否有成功。

> APP 上创建new文件夹
![WebDAV新建文件夹.gif](https://upload-images.jianshu.io/upload_images/9182331-341b75ed58ded133.gif?imageMogr2/auto-orient/strip)

> 在坚果云服务器上验证new文件夹是否存在
![web创建文件夹.png](https://upload-images.jianshu.io/upload_images/9182331-3e187c9df16b1acf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 3. 文件删除

&#160; &#160; &#160; &#160;咱们在APP上删除.normedia 文件。看看坚果云服务器上是否也对应的删除了。

> APP上删除.normedia文件
![WebDAV删除.gif](https://upload-images.jianshu.io/upload_images/9182331-403484e46ffce751.gif?imageMogr2/auto-orient/strip)

> 验证坚果云上服务器上的文件也对应的删除了。

- 4. 文件重命名

&#160; &#160; &#160; &#160;在APP上把【01】快速向导.pdf 重命名成【01】AAA.pdf

> APP重命名
![WebDAV重命名.gif](https://upload-images.jianshu.io/upload_images/9182331-6e72f159162f4566.gif?imageMogr2/auto-orient/strip)

> 验证坚果云服务器上的是否重命名成功
![web重命名.png](https://upload-images.jianshu.io/upload_images/9182331-562d90b4cf947426.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- 5. 文件移动

&#160; &#160; &#160; &#160;文件移动功能，在当前版本下发现还有些问题，等待完善。（已经定位到具体的问题出在哪里，后续有时间在改进）

- 6. 文件上传

&#160; &#160; &#160; &#160;选择手机里面的.normedia文件上传到 坚果云服务器 我的坚果云文件夹下

> 选择手机里面的.normedia文件上传
![WebDAV上传.gif](https://upload-images.jianshu.io/upload_images/9182331-533fd5778369a1a1.gif?imageMogr2/auto-orient/strip)

> 看上传的文件是否在坚果云服务器上也存在
![web文件上传.png](https://upload-images.jianshu.io/upload_images/9182331-0aa8f470b3d70dbd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 7. 文件下载

&#160; &#160; &#160; &#160;下载坚果云服务器上 我的坚果云/【01】AAA.pdf 到手机本地

> APP上选择 我的坚果云/【01】AAA.pdf 到手机本地 现在到手机里面
![WebDAV下载.gif](https://upload-images.jianshu.io/upload_images/9182331-77bf5cedb502da1b.gif?imageMogr2/auto-orient/strip)

> 验证手机里面是否存在
![web下载.png](https://upload-images.jianshu.io/upload_images/9182331-bec9d37bc23a398d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### WebDAV协议之外咱们还实现的功能

1. 文件排序
![WebDAV排序.gif](https://upload-images.jianshu.io/upload_images/9182331-6334bdb74cdce553.gif?imageMogr2/auto-orient/strip)

2. 文件筛选
![WebDAV筛选过滤.gif](https://upload-images.jianshu.io/upload_images/9182331-f298c50509081d4e.gif?imageMogr2/auto-orient/strip)

3. 上传下载列表
![WebDAV上传下载列表.gif](https://upload-images.jianshu.io/upload_images/9182331-1078dcf421453f5c.gif?imageMogr2/auto-orient/strip)


### 二，代码
&#160; &#160; &#160; &#160;关于代码的具体实现，这里咱们就不深入的追究了，里面还是挺复杂的。这里就直接给我已经实现的代码的下载地址[Android WebDAV APP](https://github.com/tuacy/WebDav)。如果大家有相同的需求的话可以扒下来研究研究。碰到啥问题也可以留言，能力范围之内尽力帮大家解决。


