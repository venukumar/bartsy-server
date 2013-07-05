<!doctype html>
<html>
<head>
</head>
<body>

	<table width="100%" border="0" cellspacing="0" cellpadding="0" class="popuptable">
		<tr>
			<td>Venue Id</td>
			<td>: ${selectedOrder.venue.venueId}
			</td>
			<td>Venue Id</td>
			<td>: ${selectedOrder.venue.venueId}
			</td>
		</tr>
		<tr>
			<td>Venue Name</td>
			<td>: ${selectedOrder.venue.venueName}
			</td>
		</tr>
		<tr>
			<td>User Name</td>
			<td>: ${selectedOrder.user.nickName}
			</td>
		</tr>
		<tr>
			<td>Bartsy Id</td>
			<td>: ${selectedOrder.user.bartsyId}
			</td>
		</tr>
		<tr>
			<td>Order Id</td>
			<td>: ${selectedOrder.orderId}
			</td>
		</tr>
		<tr>
			<td>Order Status</td>
			<td>: ${orderStatus}
			</td>
		</tr>
		<tr>
			<td>Order Last State</td>
			<td>: ${orderLastState}
			</td>
		</tr>
		<tr>
			<td>Item Id</td>
			<td>: ${selectedOrder.itemId}
			</td>
		</tr>
		<tr>
			<td>Item Name</td>
			<td>: ${selectedOrder.itemName}
			</td>
		</tr>

		<tr>
			<td>Special Instructions</td>
			<td>: ${selectedOrder.specialInstructions}
			</td>
		</tr>
		<tr>
			<td>Base Price</td>
			<td>: ${selectedOrder.basePrice}
			</td>

		</tr>
		<tr>
			<td>Tip Percentage</td>
			<td>: ${selectedOrder.tipPercentage}
			</td>
		</tr>
		<tr>
			<td>Total Price</td>
			<td>: ${selectedOrder.totalPrice}
			</td>
		</tr>
		<tr>
			<td>Order Created Time</td>
			<td>: ${selectedOrder.dateCreated}
			</td>
		</tr>

		<tr>
			<td>Order Last Updated Time</td>
			<td>: ${selectedOrder.lastUpdated}
			</td>
		</tr>

		<tr>
			<td>Recipient Nickname</td>
			<td>: ${selectedOrder.receiverProfile.nickName}
			</td>
		</tr>
		<tr>
			<td>Recipient Bartsy Id</td>
			<td>: ${selectedOrder.receiverProfile.bartsyId}
			</td>
		</tr>
		<tr>
			<td>AuthApproved</td>
			<td>: ${selectedOrder.authApproved}
			</td>
		</tr>
		<tr>
			<td>Auth Code</td>
			<td>: ${selectedOrder.authCode}
			</td>
		</tr>
		<tr>
			<td>Auth Error Message</td>
			<td>: ${selectedOrder.authErrorMessage}
			</td>
		</tr>
		<tr>
			<td>Auth Transaction Number</td>
			<td>: ${selectedOrder.authTransactionNumber}
			</td>
		</tr>

		<tr>
			<td>Capture Approved</td>
			<td>: ${selectedOrder.captureApproved}
			</td>
		</tr>

		<tr>
			<td>Capture Error Message</td>
			<td>: ${selectedOrder.captureErrorMessage}
			</td>
		</tr>

		<tr>
			<td>Capture Transaction Number</td>
			<td>: ${selectedOrder.captureTransactionNumber}
			</td>
		</tr>

		<tr>
			<td>Error Reason</td>
			<td>: ${selectedOrder.errorReason}
			</td>
		</tr>

	</table>

</body>
</html>