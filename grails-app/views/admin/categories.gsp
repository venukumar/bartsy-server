<!doctype html>
<html>
<head>
	<meta name="layout" content="main">
	<title><g:message code="categories.label" default="Categories" /></title>
</head>
<body>
	<h2><g:message code="categories.label" default="Categories" /></h2>
	<g:if test="${flash.message}">
		<div class="message" role="status">
			${flash.message}
		</div>
	</g:if>
	<% flash.clear() %>
	<div>
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
	</div>
	
	<div>
		<table>
			<tr>
				<th width="5%"><g:message code="category.label" default="Category" /></th>
				<th width="5%"><g:select id="sale" name="sale" from=""></g:select></th>
				<th width="5%"><g:message code="void.label" default="Void" /></th>
				<th width="5%"><g:message code="comps.label" default="Comps" /></th>
				<th width="5%"><g:message code="price.label" default="Price" /></th>
				<th width="5%"><g:message code="cost.label" default="Cost" /></th>
				<th width="5%"><g:message code="gross.label" default="Gross" /></th>
				<th width="5%"><g:message code="comps%.label" default="Comps%" /></th>
				<th width="5%"><g:message code="profit.label" default="Profit" /></th>
			</tr>
			<tr>	
				<td>Beverages</td>
				<td>168</td>
				<td>2</td>
				<td>1</td>
				<td>$6.19</td>
				<td>$281.63</td>
				<td>$1,136.35</td>
				<td>$44.61</td>
				<td>$719.91</td>
			</tr>
		</table>
	</div>
</body>
</html>