<%@ page import="bartsy.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="page.app.settings.title" default="App settings" /></title>
</head>
<body>
  <h1><g:message code="page.app.settings.title" default="App settings" /></h1>
<g:if test="${flash.errors}">
  <div id="msg_error" class="errors" role="status">${flash.errors}</div>
</g:if>
<g:if test="${flash.message}">
  <div id="msg_error" class="message" role="status">${flash.message}</div>
</g:if>

<% flash.clear() %>


<div class="form-con" style="width:470px">
  <g:form action="appSettings" method="post">
    <div id="timer" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="appsettings.label.timer" default="Enable Timer"/>
      </label>
      <%
      def timerreq = BartsyConfiguration.findByConfigName("timer")
      def Ychecked=""
      def Nchecked=""
      if(timerreq.value.toString().equals("true")){
        Ychecked = "checked"
      }else{
        Nchecked = "checked"
      }
      
      %>
      <div style="float:left;line-height:18px">
        <input type="radio" name="timer" id="true" value="true" ${Ychecked}/><span><g:message code="appsettings.label.yes" default="True"/></span>
      </div>
      <div style="float:right;padding-right:200px;line-height:18px">
        <input type="radio" name="timer" id="false" value="false" ${Nchecked}><g:message code="appsettings.label.no" default="False"/>
      </div>
    </div>
    
    <div id="payment" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="appsettings.label.payment" default="Payment Mode"/>
      </label>
      <%
      def paymentreq = BartsyConfiguration.findByConfigName("payment")
      def Prodchecked=""
      def Sandchecked=""
      if(paymentreq.value.toString().equals("Environment.PRODUCTION")){
        Prodchecked = "checked"
      }else{
        Sandchecked = "checked"
      }
      
      %>
      <div style="float:left;line-height:18px">
        <input type="radio" name="payment" id="true" value="Environment.SANDBOX" ${Sandchecked}/><span><g:message code="appsettings.label.sandbox" default="SandBox"/></span>
      </div>
      <div style="float:right;padding-right:173px;line-height:18px">
        <input type="radio" name="payment" id="false" value="Environment.PRODUCTION" ${Prodchecked}/><g:message code="appsettings.label.production" default="Production"/>
      </div>
    </div>
    <div id="authId" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="appsettings.authId" default="Authorize.net Id" />
      </label>
      <% def authIds = BartsyConfiguration.findByConfigName("authId") %>
      <input type="text" name="authId" id="userTimeout" value="${authIds.value }"  class="txt-field">
    </div>
    <div id="authPwd" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="appsettings.authPwd" default="Authorize.net Password" />
      </label>
      <% def authpwds = BartsyConfiguration.findByConfigName("authPassword") %>
      <input type="text" name="authPwd" id="userTimeout" value="${authpwds.value }"  class="txt-field">
    </div>
    
    <div id="userTimeout" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="appsettings.usertimeout" default="User Timeout" />
      </label>
      <% def userTime = BartsyConfiguration.findByConfigName("userTimeout") %>
      <input type="text" name="userTimeout" id="userTimeout" value="${userTime.value }"  class="txt-field">
    </div>
    <div id="venueTimeout" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="appsettings.venuetimeout" default="Venue Timeout" />
      </label>
      <% def venueTime = BartsyConfiguration.findByConfigName("venueTimeout") %>
      <input type="text" name="venueTimeout" id="venueTimeout" value="${venueTime.value }"  class="txt-field">
    </div>
     <div id="tradingDay" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="appsettings.tradingDay" default="Trading Day Time" />
      </label>
      <% 
		  def tradeTime = BartsyConfiguration.findByConfigName("tradingDay")
		  def startTime = 0, calcStartTime = 0
		  if (tradeTime){
		  	startTime = Integer.parseInt(tradeTime.value)
		  }
		  if (startTime > 12){
		  	calcStartTime = startTime - 12
		  }else{
		  	calcStartTime = startTime
		  }
      %>
      <!--  <input type="text" name="tradingDay" id="tradingDay" value="${tradeTime.value }"  class="txt-field">-->
      <select class="pick" name=tradingDay>
		<option ${(calcStartTime == 1 ? 'selected=selected': '')} value="1">01:00</option>
		<option ${(calcStartTime == 2 ? 'selected=selected': '')} value="2">02:00</option>
		<option ${(calcStartTime == 3 ? 'selected=selected': '')} value="3">03:00</option>
		<option ${(calcStartTime == 4 ? 'selected=selected': '')} value="4">04:00</option>
		<option ${(calcStartTime == 5 ? 'selected=selected': '')} value="5">05:00</option>
		<option ${(calcStartTime == 6 ? 'selected=selected': '')} value="6">06:00</option>
		<option ${(calcStartTime == 7 ? 'selected=selected': '')} value="7">07:00</option>
		<option ${(calcStartTime == 8 ? 'selected=selected': '')} value="8">08:00</option>
		<option ${(calcStartTime == 9 ? 'selected=selected': '')} value="9">09:00</option>
		<option ${(calcStartTime == 10 ? 'selected=selected': '')} value="10">10:00</option>
		<option ${(calcStartTime == 11 ? 'selected=selected': '')} value="11">11:00</option>
		<option ${(calcStartTime == 12 ? 'selected=selected': '')} value="12">12:00</option>
	  </select>
	  <select class="pick" name="tradingTime">
	  	<option ${((((startTime >= 1 && startTime <= 11) || startTime == 24)) ? 'selected=selected' : '')} value="am">AM</option>
	  	<option ${((((startTime >= 13 && startTime <= 23) || startTime == 12)) ? 'selected=selected' : '')} value="pm">PM</option>
	  </select>
    </div>
    <div style="padding:10px 0px 0px 148px;"> 
      <g:submitButton name="create" class="btn_bg" value="${message(code: 'default.button.update.label', default: 'Update')}" />
      <!--  <input type="button" name="cancel" class="ybtn" value="${message(code: 'cancelbutton.label', default: 'Cancel')}" onclick="window.location.href='${request.getContextPath()}/admin/venueList'" />-->
      <g:link action="summary" class="btn_bg" style="color: #ffffff;font-size:11px;"><g:message code="cancelbutton.label" default="Cancel" /></g:link>
    </div>
  </g:form>
</div>

</body>
</html>
