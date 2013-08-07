<!doctype html>
<html>
<head>
	<meta name="layout" content="main">
	<title><g:message code="summary.label" default="Summary" /></title>
</head>
<body>
	<h2><g:message code="summary.label" default="Summary" /></h2>
	<g:if test="${flash.message}">
		<div class="message" role="status">
			${flash.message}
		</div>
	</g:if>
	<% flash.clear() %>
	<div>
		<table class="tbl-data">
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
	
	<div class="form-con" style="width:470px">
  		<g:form action="summary" method="post">
    		<div id="totalguests" class="label-row">
      			<label style="line-height:20px;width:145px;">
        			<g:message code="summary.total.guests.label" default="Total Guests"/>
      			</label>
      			<div style="float:left;line-height:18px">
        			${totalGuests }
      			</div>
      			<label style="line-height:20px;width:145px;">
        			<g:message code="summary.total.checks.label" default="Total Checks"/>
      			</label>
      			<div style="float:left;line-height:18px">
        			${totalChecks }
      			</div>
    		</div>
    		<div id="guestavg" class="label-row">
      			<label style="line-height:20px;width:145px;">
        			<g:message code="summary.guest.avg.label" default="Guest Avg" />
      			</label>
      			<div style="float:left;line-height:18px">
        			${guestsAvg }
      			</div>
      			<label style="line-height:20px;width:145px;">
        			<g:message code="summary.check.avg.label" default="Check Avg" />
      			</label>
      			<div style="float:left;line-height:18px">
        			${checksAvg }
      			</div>
    		</div>
    		</br></br>
    		<table class="tbl-data">
			<tr>
				<th width="20%">&nbsp;</th>
				<th width="20%"><g:message code="base.label" default="Base" /></th>
				<th width="20%"><g:message code="tax.label" default="Tax" /></th>
				<th width="20%"><g:message code="comps.label" default="Comps" /></th>
				<th width="20%"><g:message code="comps%.label" default="Comps%" /></th>
				<th width="20%"><g:message code="total.label" default="Total" /></th>
			</tr>
			<tr>	
				<td><g:message code="totals.label" default="Totals" /></td>
				<td>$1</td>
				<td>$2</td>
				<td>$3</td>
				<td>%4</td>
				<td>$5</td>
			</tr>
			<tr>	
				<td><g:message code="average.label" default="Average" /></td>
				<td>$1</td>
				<td>$2</td>
				<td>$3</td>
				<td>%4</td>
				<td>$5</td>
			</tr>
			<tr>	
				<td><g:message code="per.person.label" default="Per Person" /></td>
				<td>$1</td>
				<td>$2</td>
				<td>$3</td>
				<td>%4</td>
				<td>$5</td>
			</tr>
		</table>
  		</g:form>
	</div>
</body>
</html>