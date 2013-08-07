<%@page import="bartsy.AdminUser"%>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="page.sale.list.title" default="Sales Users List" /></title>
  </head>
  <body>
  <h2>Sales Users List</h2><div style="float:right; font-weight:bold; padding-bottom:6px;"><g:link action="createSaleUser">New Sales Account</g:link></div>
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>	<% flash.clear() %>
    <div>
    <g:form method="post" controller="admin" action="saleuserList" name="frm">
    <select name="userType" id="userType" onchange="javascript:document.frm.submit();">
    <option value="">All</option>
    <option value="Admin" <% if(saleParam.userType=="Admin"){ %> selected="selected"<% } %>>Admin</option>
    <option value="SalesUser" <% if(saleParam.userType=="SalesUser"){ %> selected="selected"<% } %>>SalesUser</option>
    <option value="SalesManager" <% if(saleParam.userType=="SalesManager"){ %> selected="selected"<% } %>>SalesManager</option>
     <option value="Promoter" <% if(saleParam.userType=="Promoter"){ %> selected="selected"<% } %>>Promoter</option>
    </select>
    &nbsp;&nbsp;
    <select name="status" id="status" onchange="javascript:document.frm.submit();">
    <option value="1" <% if(saleParam.status=="1"){ %> selected="selected"<% } %>>Active</option>
    <option value="0" <% if(saleParam.status=="0"){ %> selected="selected"<% } %>>In-active</option>
    </select>
    &nbsp;
    <input type="text" name="keyword" id="keyword" value="${saleParam.keyword }">
    <input type="submit" name="search" value="Search">
    </g:form>
    </div>
	<div>
      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="tbl-data">
        <tr>
        <th width="20%"><g:message code="sale.label.name" default="Name" /></th>
		<th width="20%"><g:message code="sale.label.email" default="Email" /></th>
		<th width="20%"><g:message code="sale.label.utype" default="Account Type" /></th>
		<th width="15%">Promo Code</th>	
		<th width="15%"><g:message code="page.list.actions" default="Actions" /></th>	
        </tr>
    <% if(salesCnt>0){%>
      <g:each in="${salesList}" status="i" var="sales">
	    <tr>
          <td><div style="width:250px;word-wrap:break-word;display:block;">${sales?.firstName} ${sales?.lastName}</div></td>
          <td>${sales?.email}</td>
		  <td>${sales?.userType}</td>
	      <td>${sales?.promoterCode}</td>
          <td style="text-align:center"><table width="100%" border="0" cellspacing="0" cellpadding="0" class="actions-tbl">
 			 <tr>
   				 <td width="42%"><g:link class="edit" action="editSaleUser" id="${sales.id}"><g:message code="editbutton.title" default="Edit" /></g:link></td>
   				 <td width="58%"> <g:link class="delete" action="deleteSaleUser" id="${sales.id}"  onclick="return confirm('${message(code: 'default.button.delete.sender.message', default: 'Are you sure you wish to delete this Sales Account?')}');"><g:message code="deletebutton.title" default="Delete" /></g:link></td>
  			</tr>
		</table></td>
        </tr>
      </g:each>
      <% }else { %>
        <tr><td colspan="5" align="center"><div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div></td></tr>
      <% } %>
      </table>
    <% if(salesCnt>10){%>
      <div class="pagination">
        <g:paginate action="saleuserList" total="${salesCnt}" />
      </div>
      <% } %>
    </div>
  </body>
  </html>