/*
此代码用于实现一个聊天机器人，包含一个定时器，用于在用户发送问题后向后端发送请求并获得回复，并打印在前端页面上，以及一些用户交互功能，如余额查询和清空聊天记录等。
*/

//定义全局变量
const contextarray = [];
let uuid = randomString(32);

//在DOM加载完后执行的回调函数
$(document).ready(function () {

    let currentContent = "";
    //从 cookie 中获取用户输入的关键字，并填充到页面对应输入框中
    $("#key").val($.cookie('key'));

    //当用户在关键字输入框中输入文字时，将文字保存至 cookie 中
    $("#key").on("input", function (e) {
        $.cookie('key', e.delegateTarget.value);
    });

    //当用户在目标输入框按下回车键时，向后端发送请求进行聊天，并屏蔽回车键的默认行为
    $("#kw-target").on('keydown', function (event) {
        if (event.keyCode == 13) {
            send_post();
            return false;
        }
    });

    //当用户点击 AI 按钮时，触发与上述相同的聊天请求，并阻止默认行为
    $("#ai-btn").click(function () {
        send_post();
        return false;
    });

    //当用户点击清空按钮时，清空页面的聊天记录并弹出提示，阻止默认行为
    $("#clean").click(function () {
        $("#article-wrapper").html("");
        layer.msg("清理完毕！");
        uuid = randomString(32);
        return false;
    });

    //向页面添加聊天记录的函数，其中包含了打印输入和输出的动态效果
    function articlewrapper(answer, str) {
        //用指定的 ID 创建一个新的聊天记录元素并插入到页面的文本流中
        $("#article-wrapper").append('<li class="article-content" id="' + answer + '"><pre></pre></li>');
        //如果后端返回的字符串为空，则以默认字符串代替
        if (str == null || str == "") {
            str = "当前描述可能存在不适，或者服务器超时,未生成成功,请更换词语尝试!";
        }
        let str_ = ''
        let i = 0
        //定时器用于打印动态字符串
        let timer = setInterval(() => {
            if (str_.length < str.length) {
                str_ += str[i++]
                $("#" + answer).children('pre').text(str_ + '_')//打印时加光标
            } else {
                clearInterval(timer)
                $("#" + answer).children('pre').text(str_)//打印时加光标
            }
        }, 50)
    }

    //根据图片链接字符串添加聊天记录的函数
    function articlemapping(str) {
        if (str == null || str == "") {
            //如果返回的字符串为空，则以默认字符串代替
            $("#article-wrapper").append('<li class="article-title">我：' + prompt + '</li>');
            articlewrapper(randomString(16), '当前描述可能存在不适，或者服务器超时,未生成成功,请更换词语尝试!');
        } else {
            //向页面添加一个包含图片的聊天记录元素
            $("#article-wrapper").append('<li class="article-content"><pre><img src="' + str + '"/></pre></li>');
        }
    }

    //当用户点击查询余额按钮时，向后端发送请求查询余额，并在页面中打印结果
    $("#balance").click(function () {//查询余额
        var prompt = $("#key").val();
        if (prompt == "") {
            layer.msg("请先输入key才能查询", {icon: 5});
            return;
        }
        var loading = layer.msg('正在努力处理中,请稍后...', {
            icon: 16,
            shade: 0.4,
            time: false //取消自动关闭
        });
        $.ajax({
            cache: true,
            type: "POST",
            url: "message?balance=1",
            data: JSON.stringify({
                key: prompt,
            }),
            contentType: "application/json",
            dataType: "json",
            success: function (results) {
                layer.close(loading);
                $("#kw-target").val("");
                layer.msg("处理成功！");
                if (results.status == 1) {
                    layer.msg('当前余额:' + results.message, {
                        icon: 6,
                        time: 5000
                    });
                    $("#balance").val("查询余额 (" + results.message + ")");
                } else {
                    layer.msg(results.msg)
                }
            }
        });
        return false;
    });

    function postChat(prompt, loading) {
        $.ajax({
            cache: true,
            type: "POST",
            url: "message",
            data: JSON.stringify({
                message: prompt,
                context: $("#keep").prop("checked") ? (contextarray) : [],
                key: $("#key").val(),
                id: $("#id").val(),
            }),
            contentType: "application/json",
            dataType: "json",
            success: function (results) {
                $("#kw-target").val("");
                layer.close(loading);
                layer.msg("处理成功！");
                if ($("#id").val() == 2) {
                    if (results.raw_message == 1) {
                        //如果返回的是图片链接字符串，则调用 articlemapping 函数向页面添加图片
                        $("#article-wrapper").append('<li class="article-title">我：' + prompt + '</li>');
                        articlemapping(results.message);
                    } else {
                        //如果返回的是文本字符串，则调用 articlewrapper 函数向页面添加文字
                        $("#article-wrapper").append('<li class="article-title">我：' + prompt + '</li>');
                        articlewrapper(randomString(16), results.message);
                    }
                } else {
                    //如果是与后端保持会话，则将用户输入的问题与后端返回的回答作为一个元素加入一个数组中，并将其保存至全局变量 contextarray 中
                    contextarray.push([prompt, results.raw_message]);
                    //向页面添加聊天记录，并调用 articlewrapper 函数向其中添加后端返回的回答
                    $("#article-wrapper").append('<li class="article-title">我：' + prompt + '</li>');
                    articlewrapper(randomString(16), results.raw_message);
                }
            }
        });
    }

    function sseChat(prompt, loading) {
        //双不等号，可以避免为空值的判断
        if (!!window.EventSource) {
            let apikey = $("#key").val();
            let pro = encodeURIComponent(prompt);
            var source = new EventSource(`/chat/sse?id=${uuid}&key=${apikey}&prompt=${pro}`);//请求路径
            //收到服务器消息

            $("#ai-btn").prop("disabled", true);

            source.onmessage = function (e) {
                console.log(e.data);
                $("#kw-target").val("");
                layer.close(loading);

                let resData = JSON.parse(e.data);

                //向页面添加聊天记录，并调用   函数向其中添加后端返回的回答
                let children = $("#" + currentContent).children('pre');
                children.append(resData.content)//打印时加光标

            };


            //建立连接
            source.onopen = function (evt) {
                console.log("server connect successed");
                currentContent = randomString(16);
                //用指定的 ID 创建一个新的聊天记录元素并插入到页面的文本流中
                $("#article-wrapper").append('<li class="article-title">我：' + prompt + '</li>');
                $("#article-wrapper").append('<li class="article-content" id="' + currentContent + '"><pre></pre></li>');

            }
            //发送错误
            source.onerror = function (evt) {
                console.log(evt)
                console.log("服务器内部错误")
                $("#ai-btn").prop("disabled", false);
                source.close()
            }
        } else {
            console.log("你的浏览器不支持sse ")
        }
    }

//发起聊天请求的函数，其中包含了向页面添加聊天记录的逻辑
    function send_post() {
        //从页面获取用户输入的信息，如果为空则弹出提示并返回
        var prompt = $("#kw-target").val();
        if (prompt == "") {
            layer.msg("请输入你的 问题", {icon: 5});
            return;
        }

        //发起聊天请求前弹出等待提示
        var loading = layer.msg('正在努力处理中,请稍后...', {
            icon: 16,
            shade: 0.4,
            time: false //取消自动关闭
        });
        //向后端发送聊天请求，并在返回后将聊天记录添加到页面中
        let stream = $("#stream").prop("checked");
        if (stream) {
            sseChat(prompt, loading);
        } else {
            postChat(prompt, loading);
        }


    }

});


//生成指定长度的随机字符串的函数
function randomString(len) {
    len = len || 32;
    var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
    var maxPos = $chars.length;
    var pwd = '';
    for (i = 0; i < len; i++) {
        pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
}
