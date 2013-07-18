<%@ page import="bartsy.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="page.edit.sale.title" default="Update Sales Account" /></title>
</head>
<body>
  <h1><g:message code="page.edit.sale.title" default="Edit Sales Account" /></h1>
<g:if test="${flash.errors}">
  <div id="msg_error" class="errors" role="status">${flash.errors}</div>
</g:if>
<g:if test="${flash.message}">
  <div id="msg_error" class="message" role="status">${flash.message}</div>
</g:if>

<% flash.clear() %>


<div class="form-con" style="width:470px">
  <g:form action="editSaleUser" method="post">
  <input type="hidden" name="act" value="insert">
  <input type="hidden" name="id" value="${params.id}">
    <div id="timer" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="sale.label.fname" default="First Name"/>
      </label>
        <input type="text" name="firstName" id="firstName" value="${saleInstance?.firstName}"  class="txt-field">
    </div>
   <div id="timer" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="sale.label.lname" default="Last Name"/>
      </label>
        <input type="text" name="lastName" id="lastName" value="${saleInstance?.lastName}"  class="txt-field">
    </div>
    <div id="timer" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="sale.label.email" default="Email"/>
      </label>
        <input type="text" name="email" id="email" value="${saleInstance?.email}"  class="txt-field">
    </div>
     <div id="timer" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="sale.label.uname" default="Username"/>
      </label>
        <input type="text" name="username" id="username" value="${saleInstance?.username}"  class="txt-field" readonly="true">
    </div>
     <div id="timer" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="sale.label.pwd" default="Password"/>
      </label>
        <input type="password" name="password" id="password" value="${saleInstance?.password}"  class="txt-field">
    </div>
       <div id="timer" class="label-row">
      <label style="line-height:20px;width:145px;">
        <g:message code="sale.label.type" default="Usertype"/>
      </label>
      <%
      def timerreq = saleInstance?.userType
      def Ychecked=""
      def Nchecked=""
      if(timerreq?.toString().equals("SalesUser")){
        Ychecked = "checked"
      }else if(timerreq?.toString().equals("SalesManager")){
         Nchecked = "checked"
      }
      
      %>
      <div style="float:left;line-height:18px">
        <input type="radio" name="userType" id="userType" value="SalesManager" ${Nchecked}/><span><g:message code="sale.label.salemgr" default="SalesManager"/></span>
      </div>
      <div style="float:right;padding-right:150px;line-height:18px">
        <input type="radio" name="userType" id="userType" value="SalesUser" ${Ychecked}><g:message code="sale.label.saleusr" default="SalesUser"/>
      </div>
    </div>
    <div class="button-con"> 
      <g:submitButton name="create" class="ybtn" value="${message(code: 'default.button.update.label', default: 'Update')}" />
      <input type="button" name="cancel" class="ybtn" value="${message(code: 'cancelbutton.label', default: 'Cancel')}" onclick="window.location.href='${request.getContextPath()}/admin/saleuserList'" />
    </div>
  </g:form>
</div>

</body>
</html>
