<%@page import="bartsy.Venue"%>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="page.venue.list.title" default="Venue List" /></title>
  </head>
  <body>
  <h2>Venue List</h2>
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>	<% flash.clear() %>
	<div>
      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="tbl-data">
        <tr>
        <th width="20%"><g:message code="venue.id.label" default="Venue Id" /></th>
		<th width="20%"><g:message code="venue.name.label" default="Venue Name" /></th>
		<th width="15%"><g:message code="page.list.actions" default="Actions" /></th>	
        </tr>
    <% if(venueTotal>0){%>
      <g:each in="${venueList}" status="i" var="venue">
	    <tr>
          <td><div style="width:250px;word-wrap:break-word;display:block;">${venue.venueId}</div></td>
          <td>${venue.venueName}</td>
          <td style="text-align:center"><table width="100%" border="0" cellspacing="0" cellpadding="0" class="actions-tbl">
 			 <tr>
   				 <td width="42%"><g:link class="edit" action="edit" id="${venue.id}"><g:message code="editbutton.title" default="Edit" /></g:link></td>
   				 <td width="58%"> <g:link class="delete" action="delete" id="${venue.id}"  onclick="return confirm('${message(code: 'default.button.delete.sender.message', default: 'Are you sure you wish to delete this Venue?')}');"><g:message code="deletebutton.title" default="Delete" /></g:link></td>
  			</tr>
		</table></td>
        </tr>
      </g:each>
      <% }else { %>
        <tr><td colspan="5" align="center"><div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div></td></tr>
      <% } %>
      </table>
    <% if(venueTotal>10){%>
      <div class="pagination">
        <g:paginate total="${venueTotal}" />
      </div>
      <% } %>
    </div>
  </body>
  </html>