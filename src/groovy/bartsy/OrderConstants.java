package bartsy;

/**
 * @interfaceName OrderConstants
 * This interface contains all order related constants
 * @author Shareef
 *
 */
public interface OrderConstants {
	
	// Order status codes
	String ORDER_STATUS_NEW						= "0";  
	String ORDER_STATUS_REJECTED				= "1";  
	String ORDER_STATUS_ACCEPTED				= "2";  
	String ORDER_STATUS_COMPLETE				= "3";  
	String ORDER_STATUS_FAILED					= "4";  
	String ORDER_STATUS_PICKED_UP				= "5";  
	String ORDER_STATUS_NOSHOW					= "6";  
	String ORDER_STATUS_ORDER_TIMEOUT			= "7";  // OrderTimeout or when placing authApproved is false we are setting order status to 7
	String ORDER_STATUS_OFFERED_DRINK_REJECTION	= "8";  
	String ORDER_STATUS_OFFERED_DRINK			= "9";  
	String ORDER_STATUS_PAST_ORDER				= "10";  
	String ORDER_STATUS_100						= "100";
	
	String ORDER_STATUS				= "orderStatus";
	String ORDER_ID					= "orderId";
	String ORDER_TIME				= "orderTime";
	String ORDER_TIMEOUT			= "orderTimeout";
	String ORDER_COUNT				= "orderCount";
	String ORDER_PLACED				= "Order Placed";
	String ORDER_TYPE_SELF			= "self";
}
