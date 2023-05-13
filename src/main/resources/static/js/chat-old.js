/*
//定时器*/
const contextarray = [];
$(document).ready(function () {

    $("#key").val($.cookie('key'));

    $("#key").on("input", function (e) {
        $.cookie('key', e.delegateTarget.value);
    });

    $("#kw-target").on('keydown', function (event) {
        if (event.keyCode == 13) {
            send_post();
            return false;
        }
    });
    $("#ai-btn").click(function () {


        send_post();
        return false;
    });
    $("#clean").click(function () {
        $("#article-wrapper").html("");
        layer.msg("清理完毕！");
        return false;
    });

    function articlewrapper(answer, str) {
        $("#article-wrapper").append('<li class="article-content" id="' + answer + '"><pre></pre></li>');
        if (str == null || str == "") {
            str = "当前描述可能存在不适，或者服务器超时,未生成成功,请更换词语尝试!";
        }
        let str_ = ''
        let i = 0
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

    function articlemapping(str) {
        if (str == null || str == "") {
            $("#article-wrapper").append('<li class="article-title">Me：' + prompt + '</li>');
            articlewrapper(randomString(16), '当前描述可能存在不适，或者服务器超时,未生成成功,请更换词语尝试!');
        } else {
            $("#article-wrapper").append('<li class="article-content"><pre><img src="' + str + '"/></pre></li>');
        }
    }

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
                } else {
                    layer.msg(results.msg)
                }
            }
        });
        return false;
    });

    function send_post() {

        var prompt = $("#kw-target").val();
        if (prompt == "") {
            layer.msg("请输入你的 问题", {icon: 5});
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
                layer.close(loading);
                $("#kw-target").val("");
                layer.msg("处理成功！");
                if ($("#id").val() == 2) {
                    if (results.raw_message == 1) {
                        $("#article-wrapper").append('<li class="article-title">Me：' + prompt + '</li>');
                        articlemapping(results.message);
                    } else {
                        $("#article-wrapper").append('<li class="article-title">Me：' + prompt + '</li>');
                        articlewrapper(randomString(16), results.message);
                    }
                } else {
                    contextarray.push([prompt, results.raw_message]);
                    $("#article-wrapper").append('<li class="article-title">Me：' + prompt + '</li>');
                    articlewrapper(randomString(16), results.raw_message);
                }
            }
        });
    }

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
});
