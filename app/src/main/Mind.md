这个APP所有组件使用Material3-MDC，所有组件使用MDC设计，语言为java
现在我们完成MainActivity的布局
布局文件为activity_main.xml
顶部是一个Top app bars，显示当前Fragment的标题
底部是一个Bottom navigation，显示当前Fragment的图标和名称
现在我们新增三个Fragment，分别是HomeFragment、ControlFragment、SettingsFragment
HomeFragment的布局文件为fragment_home.xml
ControlFragment的布局文件为fragment_control.xml
SettingsFragment的布局文件为fragment_settings.xml

现在我们完成MainActivity，包名为com.sakongapps.smartraily，你必须在相应文件夹下完成，启动后默认显示HomeFragment

java代码应该放到 java/com/sakongapps/smartraily 文件夹下

我们这个APP是用来串口通信的，在HomeFragment中，添加一个连接状态Card，显示当前的串口连接状态，连接状态有：
- 未连接
- 已连接

在未连接的时候，我们点击卡片，弹出对话框，选择已配对的蓝牙设备，选择后，连接蓝牙设备的串口，连接成功后，卡片显示已连接和连接的设备名称和蓝牙Mac地址

你需要新建一个蓝牙SDK，所有蓝牙操作都必须只在蓝牙SDK中完成，不得在其他地方完成，其他地方只能调用蓝牙SDK的接口

在HomeFragment中，添加一个日志信息卡片，显示串口所有的通信数据，不受到切换Fragment的影响，日志信息卡片显示的数据需要包括时间，方向，内容

在ControlFragment中，这个页面用来使用快捷指令，在切换到这个Fragment后，在Top app bars中的右侧添加一个添加按钮，用来添加快捷指令卡片

点击添加按钮后，弹出对话框，输入快捷指令的名称，然后输入快捷指令的内容，然后点击确定按钮，添加到快捷指令卡片列表中，快捷指令卡片列表显示在内容区域，一行两个，点击后可以快速发送卡片代表的指令

现在在设置增加一个条目，为：调试模式，点击后可以打开ControlFragment

现在我们需要把ControlFragment中的所有内容移动到一个新的Fragment中，即DebugFragment，设置中的调试模式点击后打开DebugFragment，而不再打开ControlFragment

将ControlFragment改名为手动模式，在手动模式和设置中间新增一个导航选项，为自动模式，对应的Fragment为AutoFragment

手动模式页面添加两个模式的入口：
        远程操控模式
        协同操控模式
这个页面尽量美观

同样的，在自动模式实现两个模式的入口
智能模式：
          常规巡检模式
           应急巡检模式
           
自动模式页面尽量美观

在设置页面添加一个类似调试模式的关于APP的卡片条目，点击后弹出关于APP的对话框，对话框中显示APP的名称、版本、开发者、以及关于APP的描述
