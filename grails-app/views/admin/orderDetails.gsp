<!doctype html>
<html>
<head>
</head>
<body>

	<table width="100%" border="0" cellspacing="0" cellpadding="0" class="popuptable">
		<tr>
			<td>Venue Id</td>
			<td>:</td><td> ${selectedOrder.venue.venueId}
			</td>
			<td>Venue Name</td>
			<td>:</td><td> ${selectedOrder.venue.venueName}
			</td>
		</tr>
		<tr>
			<td>User Name</td>
			<td>:</td><td> ${selectedOrder.user.nickName}
			</td>
			<td>Bartsy Id</td>
			<td>:</td><td>${selectedOrder.user.bartsyId}
			</td>
		</tr>
		<tr>
			<td>Order Id</td>
			<td>:</td><td> ${selectedOrder.orderId}
			</td>
			<td>Order Status</td>
			<td>:</td><td> ${orderStatus}
			</td>
		</tr>
		<tr>
			<td>Order Last State</td>
			<td>:</td><td>${orderLastState}
			</td>
		</tr>
		<tr>
			<td>Item Id</td>
			<td>:</td><td>${selectedOrder.itemId}
			</td>
			<td>Item Name</td>
			<td>:</td><td>${itemName}
			</td>
		</tr>

		<tr>
			<td>Special Instructions</td>
			<td>:</td><td>${selectedOrder.specialInstructions}
			</td>
			<td>Base Price</td>
			<td>:</td><td>$ ${selectedOrder.basePrice}
			</td>

		</tr>
		<tr>
			<td>Tip Percentage</td>
			<td>:</td><td>${selectedOrder.tipPercentage}
			</td>
			<td>Total Price</td>
			<td>:</td><td>$ ${selectedOrder.totalPrice}
			</td>
		</tr>
		<tr>
			<td>Ordered date</td>
			<td>:</td><td>${selectedOrder.dateCreated}
			</td>
		</tr>
		<tr>
			<td>Recipient Nickname</td>
			<td>:</td><td>${selectedOrder.receiverProfile.nickName}
			</td>
		
			<td>Recipient Bartsy Id</td>
			<td>:</td><td>${selectedOrder.receiverProfile.bartsyId}
			</td>
		</tr>
		<tr>
			<td>AuthApproved</td>
			<td>:</td><td>${selectedOrder.authApproved}
			</td>
		
			<td>Auth Code</td>
			<td>:</td><td>${selectedOrder.authCode}
			</td>
		</tr>
		<tr>
			<td>Auth Error Message</td>
			<td>:</td><td>${selectedOrder.authErrorMessage}
			</td>
		
			<td>Auth Transaction Number</td>
			<td>:</td><td>${selectedOrder.authTransactionNumber}
			</td>
		</tr>

		<tr>
			<td>Capture Approved</td>
			<td>:</td><td>${selectedOrder.captureApproved}
			</td>
		
			<td>Capture Error Message</td>
			<td>:</td><td>${selectedOrder.captureErrorMessage}
			</td>
		</tr>

		<tr>
			<td>Capture Transaction Number</td>
			<td>:</td><td>${selectedOrder.captureTransactionNumber}
			</td>
		
			<td>Error Reason</td>
			<td>:</td><td>${selectedOrder.errorReason}
			</td>
		</tr>

	</table>

</body>
</html>