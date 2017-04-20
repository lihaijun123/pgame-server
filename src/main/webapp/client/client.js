/**
 * 
 * websocket客户端
 * 
 */
var wsclient = (function($){
	var url = "ws://172.17.13.77:8080/websocket";
	var _this = {};
	
	var socket;
	if (!window.WebSocket) {
		window.WebSocket = window.MozWebSocket;
	}
	if (window.WebSocket) {
		socket = new WebSocket(url);
		//服务器响应处理
		socket.onmessage = function (event) {
			var response = event.data;
			var restext = jQuery.parseJSON(response);
			var ta = document.getElementById('responseText');
			ta.value = "";
			ta.value = response;
			if(restext.type === 1){
				$("#groupId").val(restext.groupId);
			}
			if(restext.type === 2){
				$("#groupId").val(restext.groupId);
			}
			if(restext.type === 3){
				$("#chatMsgTt").val(restext.body);
			}
		};
		//链接成功
		socket.onopen = function (event) {
			var ta = document.getElementById('responseText');
			ta.value = "打开WebSocket服务正常，浏览器支持WebSocket!";
		};
		//关闭连接
		socket.onclose = function (event) {
			var ta = document.getElementById('responseText');
			ta.value = "";
			ta.value = "WebSocket 关闭!";
		};
	}
	else {
		alert("抱歉，您的浏览器不支持WebSocket协议!");
	}
	/**
	 * 发送消息
	 * 协议：{"type":"", "groupId":"", "body":""}
	 * type取值：1-新建组 2-加入组，3-群消息
	 * groupId：可以为空
	 * body：可以为空
	 * 
	 */
	function send(message) {
		if (!window.WebSocket) {
			return;
		}
		if (socket.readyState == WebSocket.OPEN) {
			console.info(JSON.stringify(message));
			socket.send(JSON.stringify(message));
		}
		else {
			alert("WebSocket连接没有建立成功!");
		}
	}
	
	_this.send = send;
	
	return _this;
})(jQuery);
