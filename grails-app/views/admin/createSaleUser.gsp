<%@ page import="bartsy.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="page.create.title" default="Create Account" /></title>
</head>
<body>
  <h1 style="border-bottom:1px solid #e7e7e7;"><g:message code="page.create.title" default="Create Account" /></h1>
<g:if test="${flash.errors}">
  <div id="msg_error" class="errors" role="status">${flash.errors}</div>
</g:if>
<g:if test="${flash.message}">
  <div id="msg_error" class="message" role="status">${flash.message}</div>
</g:if>

<% flash.clear() %>


<div class="form-con" style="width:470px">
  <g:form action="createSaleUser" method="post">
  
  <div id="timer" class="label-row">
      <label style="line-height:30px;width:145px;">
        <g:message code="sale.label.type" default="Usertype"/>
      </label>
	  <select name="userType" id="userType">
    	<option value="Admin" <% if(saleInstance?.userType=="Admin"){ %> selected="selected"<% } %>><g:message code="admin.label" default="Admin" /></option>
    	<option value="VendsySalesManager" <% if(saleInstance?.userType=="VendsySalesManager"){ %> selected="selected"<% } %>><g:message code="vendsy.sales.manager" default="Vendsy Sales Manager" /></option>
    	<option value="VenueManager" <% if(saleInstance?.userType=="VenueManager"){ %> selected="selected"<% } %>><g:message code="venue.manager" default="Venue Manager" /></option>
     	<option value="Promoter" <% if(saleInstance?.userType=="Promoter"){ %> selected="selected"<% } %>><g:message code="promoter" default="Promoter" /></option>
   	  </select>
   </div>
    
  <input type="hidden" name="act" value="insert">
    <div id="timer" class="label-row">
      <label style="line-height:30px;width:145px;">
        <g:message code="sale.label.fname" default="First Name"/>
      </label>
        <input type="text" name="firstName" id="firstName" value="${saleInstance.firstName}"  class="txt-field">
    </div>
   <div id="timer" class="label-row">
      <label style="line-height:30px;width:145px;">
        <g:message code="sale.label.lname" default="Last Name"/>
      </label>
        <input type="text" name="lastName" id="lastName" value="${saleInstance.lastName}"  class="txt-field">
    </div>
    <div id="timer" class="label-row">
      <label style="line-height:30px;width:145px;">
        <g:message code="sale.label.email" default="Email"/>
      </label>
        <input type="text" name="email" id="email" value="${saleInstance.email}"  class="txt-field">
    </div>
     <div id="timer" class="label-row">
      <label style="line-height:30px;width:145px;">
        <g:message code="sale.label.uname" default="Username"/>
      </label>
        <input type="text" name="username" id="username" value="${saleInstance.username}"  class="txt-field">
    </div>
     <div id="timer" class="label-row">
      <label style="line-height:30px;width:145px;">
        <g:message code="sale.label.pwd" default="Password"/>
      </label>
        <input type="password" name="password" id="password" value="${saleInstance.password}"  class="txt-field">
    </div>
       
      <%--
      def timerreq = saleInstance?.userType
      def Ychecked=""
      def Nchecked=""
      if(timerreq?.toString().equals("SalesUser")){
        Ychecked = "checked"
      }else if(timerreq?.toString().equals("SalesManager")){
         Nchecked = "checked"
      }
      
      --%>
      <!-- <div style="float:left;line-height:18px">
        <input type="radio" name="userType" id="userType" value="SalesManager" ${Nchecked}/><span><g:message code="sale.label.salemgr" default="SalesManager"/></span>
      </div>
      <div style="float:right;padding-right:150px;line-height:18px">
        <input type="radio" name="userType" id="userType" value="SalesUser" ${Ychecked}><g:message code="sale.label.saleusr" default="SalesUser"/>
      </div> -->
      
    <div class="button-con" style="padding:10px 0px 0px 148px;"> 
      <g:submitButton name="create" class="ybtn" value="${message(code: 'default.button.create.label', default: 'Create')}" />
      <%--<input type="button" name="cancel" class="ybtn" value="${message(code: 'cancelbutton.label', default: 'Cancel')}" onclick="window.location.href='${request.getContextPath()}/admin/saleuserList'" />--%>
      <g:link action="saleuserList" class="btn_bg" style="color: #ffffff;font-size:11px;"><g:message code="cancelbutton.label" default="Cancel" /></g:link>
    </div>
  </g:form>
</div>

</body>
</html>
