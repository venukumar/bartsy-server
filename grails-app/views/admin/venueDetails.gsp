<!doctype html>
<html>
<head>
</head>
<body>

	<table class="popuptable">
		<tr>
			<td><g:message code="name.label" default="Name" /></td>
			<td>:</td><td>${venue?.venueName}</td>
			<td><g:message code="address.label" default="Address" /></td>
			<td>:</td><td>${venue?.address}</td>
		</tr>
		<tr>
			<td><g:message code="hours.label" default="Hours" /></td>
			<td>:</td><td>${venue?.openHours}</td>
			<td><g:message code="manager.name.label" default="Manager Name" /></td>
			<td>:</td><td>${venue?.managerName}</td>
		</tr>
		<tr>
			<td><g:message code="venue.login.label" default="Venue Login" /></td>
			<td>:</td><td>${venue?.venueLogin}</td>
			<td><g:message code="venue.password.label" default="Venue Password" /></td>
			<td>:</td><td>${venue?.venuePassword}</td>
		</tr>
		<tr>
			<td><g:message code="phone.number.label" default="Phone Number" /></td>
			<td>:</td><td>${venue?.phoneNumber}</td>
			<td><g:message code="vendsy.rep.name.label" default="Vendsy Representative Name" /></td>
			<td>:</td><td>${venue?.vendsyRepName}</td>
		</tr>
		<tr>
			<td><g:message code="vendsy.rep.email.label" default="Vendsy Representative Email" /></td>
			<td>:</td><td>${venue?.vendsyRepEmail}</td>
			<td><g:message code="vendsy.rep.phone.label" default="Vendsy Representative Phone" /></td>
			<td>:</td><td>${venue?.vendsyRepPhone}</td>
		</tr>
		<tr>
			<td><g:message code="locuusername.label" default="Locu username" /></td>
			<td>:</td><td>${venue?.locuUsername}</td>
			<td><g:message code="locupwd.label" default="Locu password" /></td>
			<td>:</td><td>${venue?.locuPassword}</td>
		</tr>
		<tr>
			<td><g:message code="locuid.label" default="Locu ID" /></td>
			<td>:</td><td>${venue?.locuId}</td>
			<td><g:message code="locusection.label" default="Locu section" /></td>
			<td>:</td><td>${venue?.locuSection}</td>
		</tr>
		<tr>
			<td><g:message code="ordertimeout.label" default="Order timeout" /></td>
			<td>:</td><td>${venue?.cancelOrderTime}</td>
			<td><g:message code="totaltaxrate.label" default="Total tax rate" /></td>
			<td>:</td><td>${venue?.totalTaxRate}</td>
		</tr>
		<tr>
			<td><g:message code="routingnumber.label" default="Routing Number" /></td>
			<td>:</td><td>${venue?.routingNumber}</td>
		</tr>
		<tr>
			<td><g:message code="accountnumber.label" default="Account Number" /></td>
			<td>:</td><td>${venue?.accountNumber}</td>
			<td><g:message code="wifipresent.label" default="Wifi Present" /></td>
			<td>:</td><td>${venue?.wifiPresent.equals("1")?'On':'Off'}</td>
		</tr>
		<tr>
			<td><g:message code="wifiname.label" default="Wifi Name" /></td>
			<td>:</td><td>${venue?.wifiName}</td>
			<td><g:message code="wificode.label" default="Wifi Code" /></td>
			<td>:</td><td>${venue?.wifiPassword}</td>
		</tr>
		<tr>
			<td><g:message code="authentication.label" default="Authentication" /></td>
			<td>:</td><td>${venue?.typeOfAuthentication}</td>
			<td><g:message code="networktype.label" default="Network Type" /></td>
			<td>:</td><td>${venue?.wifiNetworkType}</td>
		</tr>
	</table>

</body>
</html>