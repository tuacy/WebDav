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

代码都是为了实现功能而写的，很多地方估计都会有问题，有很多情况都没有考虑，如果你编译之后显示不了WebDAV服务器端的文件列表，你可以自己找下原因多加些打印语句注意下路径中的反斜杠的文件有没有多了少了什么的或者用Fiddler4工具去抓下看下每个请求返回来的数据是什么，因为有些是没有按照WebDAV协议返回数据的就要在代码里面做相应的改动了。
