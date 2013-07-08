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
    <div class="button-con"> 
      <g:submitButton name="create" class="ybtn" value="${message(code: 'default.button.update.label', default: 'Update')}" />
      <input type="button" name="cancel" class="ybtn" value="${message(code: 'cancelbutton.label', default: 'Cancel')}" onclick="window.location.href='${request.getContextPath()}/admin/venueList'" />
    </div>
  </g:form>
</div>

</body>
</html>