<%@page import="bartsy.Orders"%>
<!doctype html>
<html>
<head>
<meta name="layout" content="main">
<title><g:message code="page.order.list.title"
		default="Orders List" /></title>
</head>
<body>
	<h2>Orders List</h2>
	<div style="text-align: right;">
		<g:link action="downloadCSV">Download CSV</g:link>
	</div>
	<g:if test="${flash.message}">
		<div class="message" role="status">
			${flash.message}
		</div>
	</g:if>
	<% flash.clear() %>
	<div>
		<table width="100%" border="0" cellspacing="0" cellpadding="0"
			class="tbl-data">
			<tr>
				<th width="10%"><g:message code="order.id.label"
						default="Order Id" /></th>
				<th width="20%"><g:message code="item.name.label"
						default="Item Name" /></th>
				<th width="20%"><g:message code="order.uname.label"
						default="Ordered By" /></th>
				<th width="20%"><g:message code="venue.name.label"
						default="Venue" /></th>
				<th width="20%"><g:message code="order.date.label"
						default="Ordered Date" /></th>
				<th width="10%"><g:message code="order.total.label"
						default="Order Total" /></th>
			</tr>
			<% if(ordersTotal>0){%>
      <g:each in="${ordersList}" status="i" var="orderInfo">
	  
	  <%
	  def orderId = orderInfo.orderId
	  def itemName = itemsNames.get(orderId)
	  def price = orderInfo.totalPrice.toString()
	  if(price.contains(".")){
		
		  	  }
	  %>
	    <tr>
          <td><modalbox:createLink controller="admin" action="orderDetails" id="${orderInfo.orderId}" title="Show Order!" width="750">${orderInfo.orderId}</modalbox:createLink></td>
          <td>${itemName}</td>
          <td>${orderInfo.user.nickName}</td>
	  	  <td>${orderInfo.venue.venueName}</td>
		  <td>${orderInfo.dateCreated.format('hh:mm a, MMM dd,yyyy')}</td>
	  	  <td>$ ${price}</td>
        </tr>
      </g:each>
      <% }else { %>
        <tr><td colspan="5" align="center"><div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div></td></tr>
      <% } %>
		</table>
		<% if(ordersTotal>50){%>
      <div class="pagination">
       <g:paginate action="ordersList" total="${ordersTotal}" />
      </div>
      <% } %>
	</div>
</body>
</html>