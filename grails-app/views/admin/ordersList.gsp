<%@page import="bartsy.Orders"%>
<!doctype html>
<html>
<head>
	<meta name="layout" content="main">
	<title><g:message code="page.order.list.title" default="Orders List" /></title>
	<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
  <script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
   <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
	<script type="text/javascript">
	$(function() {
	    $( "#datepicker" ).datepicker();
	    $( "#datepicker1" ).datepicker();
	  });
	</script>
</head>
<body>
	<h2><g:message code="page.order.list.title" default="Orders List" /></h2>
	<!-- <div style="text-align: right;">
		<g:link action="downloadCSV">Download CSV</g:link>
	</div> -->
	<g:if test="${flash.message}">
		<div class="message" role="status">
			${flash.message}
		</div>
	</g:if>
	<% flash.clear() %>
<fieldset>
 <g:form method="post" controller="admin" action="ordersList">
Start Date: <input type="text" id="datepicker" name="startDate"  value="${jqStart }"/>&nbsp; End Date: <input type="text" id="datepicker1" name="endDate" value="${jqEnd }" />
<input type="submit" value="search"> </g:form>
</fieldset>
	<!-- <div>
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="tbl-data">
			<tr>
				<th width="10%"><g:link controller="admin" action="summary"><g:message code="summary.label" default="Summary" /></g:link></th>
				<th width="20%"><g:link controller="admin" action="categories"><g:message code="categories.label" default="Categories" /></g:link></th>
				<th width="20%"><g:link controller="admin" action="ordersList"><g:message code="items.label" default="Items" /></g:link></th>
				<th width="20%"><g:message code="guests.label" default="Guests" /></th>
				<th width="20%">Time</th>
				<th>
					<div style="text-align: right;">
						<g:link action="downloadCSV">PDF</g:link>
					</div>
				</th>
				<th>
					<div style="text-align: right;">
						<g:link action="downloadCSV">CSV</g:link>
					</div>
				</th>
			</tr>
		</table>
	</div> -->
	<div>
		<table class="tbl-data">
			<tr>
				<th width="10%"><g:message code="time.label" default="Time" /></th>
				<th width="6%"><g:message code="transaction.id.label" default="Transaction Id" /></th>
				<th width="20%"><g:message code="item.label" default="Item" /></th>
				<th width="12%"><g:message code="sender.label" default="Sender" /></th>
				<th width="12%"><g:message code="recipient.label" default="Recipient" /></th>
				<th width="8%"><g:message code="gross.label" default="Gross" /></th>
				<th width="8%"><g:message code="tax.label" default="Tax" /></th>
				<th width="8%"><g:message code="comp.label" default="Comp" /></th>
				<th width="8%">%</th>
				<th width="10%"><g:message code="net.label" default="Net" /></th>
			</tr>
			<%
			if(ordersTotal>0){
			%>
      			<g:each in="${ordersList}" status="i" var="orderInfo">
	  		<%
	  			def orderId = orderInfo.orderId
	  			def itemName = itemsNames.get(orderId)
				def gross = gross.get(orderId)
				def tax = tax.get(orderId)
				def tipPercentage = tip.get(orderId)
				def comp = comp.get(orderId)
				def net = net.get(orderId)
	  		%>
	    	<tr>
				<td>${orderInfo.dateCreated}</td>
          		<td><modalbox:createLink controller="admin" action="orderDetails" id="${orderInfo.orderId}" title="Show Order!" width="750">${orderInfo.orderId}</modalbox:createLink></td>
          		<td>${itemName}</td>
          		<td>${orderInfo.user.nickName}</td>
				<td>${orderInfo.receiverProfile.nickName}</td>
	  	  		<td>${gross}</td>
				<td>${tax}</td>
			    <td>${comp}</td>
				<td>${tipPercentage}</td>
		  		<td>${net}</td>
        	</tr>
      			</g:each>
      		<% }else { %>
        	<tr>
				  <td colspan="10" align="center">
				  	<div class="errors"><g:message code="page.list.not.found" default="No Records Found" /></div>
				  </td>
			</tr>
      		<% } %>
		</table>
		<hr/>
		<%if(ordersTotal>0){
			def totalGuests = totalGuests.get("totalGuests")
			// Totals
			def grossTotal = grossTotal.get("grossTotal")
			def taxTotal = taxTotal.get("taxTotal")
			def compTotal = compTotal.get("compTotal")
			def percentageTotal = percentageTotal.get("percentageTotal")
			def netTotal = netTotal.get("netTotal")
			// Average
			def avgGrossTotal = avgGrossTotal.get("avgGrossTotal")
			def avgTaxTotal = avgTaxTotal.get("avgTaxTotal")
			def avgCompTotal = avgCompTotal.get("avgCompTotal")
			def avgNetTotal = avgNetTotal.get("avgNetTotal")
			// Per guest
			def perGuestGrossTotal = perGuestGrossTotal.get("perGuestGrossTotal")
			def perGuestTaxTotal = perGuestTaxTotal.get("perGuestTaxTotal")
			def perGuestCompTotal = perGuestCompTotal.get("perGuestCompTotal")
			def perGuestNetTotal = perGuestNetTotal.get("perGuestNetTotal")
		%>
		<table class="tbl-data">
			<tr>
				<td width="10%"><g:message code="total.checks.label" default="Total Checks" /></td>
				<td width="6%">${ordersTotal}</td>
				<td width="20%"></td>
				<td width="12%"></td>
				<td width="12%"><g:message code="totals.label" default="Totals" /></td>
				<td width="8%">${grossTotal}</td>
				<td width="8%">${taxTotal}</td>
				<td width="8%">${compTotal}</td>
				<td width="8%">${percentageTotal}</td>
				<td width="10%">${netTotal}</td>
			</tr>
			<tr>
				<td width="10%"><g:message code="total.guests.label" default="Total Guests" /></td>
				<td width="6%">${totalGuests}</td>
				<td width="20%"></td>
				<td width="12%"></td>
				<td width="12%"><g:message code="average.label" default="Average" /></td>
				<td width="8%">${avgGrossTotal}</td>
				<td width="8%">${avgTaxTotal}</td>
				<td width="8%">${avgCompTotal}</td>
				<td width="8%">${percentageTotal}</td>
				<td width="10%">${avgNetTotal}</td>
			</tr>
			<tr>
				<td width="10%"><g:message code="checks.guests.label" default="Checks/Guests" /></td>
				<td width="6%">${ordersTotal/totalGuests}</td>
				<td width="20%"></td>
				<td width="12%"></td>
				<td width="12%"><g:message code="per.guest.label" default="Per Guest" /></td>
				<td width="8%">${perGuestGrossTotal}</td>
				<td width="8%">${perGuestTaxTotal}</td>
				<td width="8%">${perGuestCompTotal}</td>
				<td width="8%">${percentageTotal}</td>
				<td width="10%">${perGuestNetTotal}</td>
			</tr>
		</table>
		<%}%>
		<% if(ordersTotal>50){%>
      		<div class="pagination">
       			<g:paginate action="ordersList" total="${ordersTotal}" />
				   
      		</div>
      	<% } %>
      	<%if(ordersTotal>0){ %>
      	<export:formats formats="['csv', 'excel', 'pdf']" />
      	<% } %>	
	</div>
</body>
</html>