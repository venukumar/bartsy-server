package bartsy

import java.text.SimpleDateFormat
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.web.json.JSONArray

class CommonMethods {



	/**
	 * To verify the user profile exists or not
	 */
	def verifyBartsyId(bartsyId){
		boolean user = false
		def userProfile = UserProfile.findByBartsyId(bartsyId)
		if(userProfile)
			user=true
		return user
	}
	def exceptionFound(e,response){
		println"Exception found in getUserRewards "+e.getMessage()
		response.put("errorCode",200)
		response.put("errorMessage",e.getMessage())
	}


	def getNotifictionCount(UserProfile user){
		def unReadNotifications = Notifications.findAllByUserAndStatus(user,0)
		if(unReadNotifications){
			return unReadNotifications.size()
		}else{
			return 0
		}
	}


	/* Checking String is Integer or not*/

	public boolean isInteger(String integerString){

		try{
			if(integerString.indexOf(".") > 0 ){
				Double.parseDouble(integerString);
				return true;
			}else{
				Integer.parseInt(integerString);
				return true;
			}
		} catch (NumberFormatException nfe) {
			return false;
		}
	}


	/**
	 * 
	 * To calculate the age based on DOB
	 * 
	 * @param dob
	 * @return
	 */
	def getAge(String dob){
		// Date is in YYYY-MM-DD format.
		int yearDOB = Integer.parseInt(dob.substring(0, 4));
		int monthDOB = Integer.parseInt(dob.substring(5, 7));
		int dayDOB = Integer.parseInt(dob.substring(8, 10));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date date = new Date();
		int thisYear = Integer.parseInt(dateFormat.format(date));
		dateFormat = new SimpleDateFormat("MM");
		date = new java.util.Date();
		int thisMonth = Integer.parseInt(dateFormat.format(date));
		dateFormat = new SimpleDateFormat("dd");
		date = new java.util.Date();
		int thisDay = Integer.parseInt(dateFormat.format(date));
		int age = thisYear-yearDOB;
		if(thisMonth < monthDOB){
			age = age-1;
		}
		if(thisMonth == monthDOB && thisDay < dayDOB){
			age = age-1;
		}
		System.out.println("The age of user is : " + age);
		return age
	}
	static final String num = "123456789";
	static Random rnum = new Random()
	/* 
	 * This is the method used for to generate random number
	 * 
	 */
	String randomNumString( int len ) {
		StringBuilder sb = new StringBuilder( len );
		for( int i = 0; i < len; i++ ){
			sb.append( num.charAt( rnum.nextInt(num.length()) ) );}
		return sb.toString();
	}
	/*
	 * This is the method used for to generate promoter code
	 *
	 */
	String promoCode(int length) {
		String charset = (('A'..'Z') + ('0'..'0')).join()
		String randomString = RandomStringUtils.random(length, charset.toCharArray())
		return randomString
	}

	/**
	 * 
	 * This method used to get orders and venues details of user
	 * 
	 * @param venue
	 * @param checkedInUser
	 * @param checkedInUsersMap
	 * @return
	 */

	def getUserOrderAndChekedInDetails(Venue venue,UserProfile checkedInUser,checkedInUsersMap){
		// getting orders based on venue and bartsy id(user)
		def totalOrdersOrderedByUser = Orders.findAllByUserAndVenue(checkedInUser,venue)
		// current date
		def date = new Date()
		// last 30th date
		def last30thdate = date-30

		println "date "+date
		println "last30thdate "+last30thdate
		if(totalOrdersOrderedByUser)
		{
			def last30DaysOrdersOrderedByuser = Orders.createCriteria()

			def last30daysCount = last30DaysOrdersOrderedByuser.list(){
				and{
					eq("venue",venue)
					eq("user",checkedInUser)
				}
				gt("dateCreated",last30thdate)
				order("dateCreated", "desc")
			}
			println"middle order cc"

			def firstOrderDate =  Orders.createCriteria().list{
				eq("venue",venue)
				eq("user",checkedInUser)

				order("dateCreated", "asc")
			}
			println "firstOrderDate[0].dateCreated "+firstOrderDate[0].dateCreated
			checkedInUsersMap.put("firstOrderDate",firstOrderDate?firstOrderDate[0].dateCreated:"")
			checkedInUsersMap.put("orderCount",totalOrdersOrderedByUser.size())
			checkedInUsersMap.put("last30DaysOrderCount",last30daysCount?last30daysCount.size():"")

		}
		// getting checkedIn details of user based on venue
		def userCheckedInDetails = UserCheckInDetails.findAllByUserProfileAndVenue(checkedInUser,venue)
		if(userCheckedInDetails){
			def last30DaysCheckInCount = UserCheckInDetails.createCriteria()
			def last30DaysCheckIns = last30DaysCheckInCount.list(){
				and{
					eq("venue",venue)
					eq("userProfile",checkedInUser)
				}
				gt("checkedInDate",last30thdate)
				order("checkedInDate", "asc")
			}

			def firstCheckIn = UserCheckInDetails.createCriteria().list{
				and{
					eq("venue",venue)
					eq("userProfile",checkedInUser)
				}
				order("checkedInDate", "asc")
			}
			println "firstCheckIn[0].checkedInDate "+firstCheckIn[0].checkedInDate
			checkedInUsersMap.put("firstCheckInDate",firstCheckIn?firstCheckIn[0].checkedInDate:"")
			checkedInUsersMap.put("checkInCount",userCheckedInDetails.size())
			checkedInUsersMap.put("last30DaysCheckInCount",last30DaysCheckIns?last30DaysCheckIns.size():"")
			println"end !!!!! "
		}
		//return checkedInUsersMap
	}
	/*
	 *  Calculating reward points of the user based on the venue
	 */
	def calculateRewardPoints(Orders order){

		if(order.user.emailVerified.toString().equalsIgnoreCase("true")){
			int rewards=1
			if(order.itemsList){
				def itemsListStr = new JSONArray(order.itemsList)
				rewards = itemsListStr.size()
			}


			def userRewardsPoints
			//userRewardsPoints =  UserRewardPoints.findByUserAndVenue(user,venue)

			//if(userRewardsPoints){
			//	int existingPoints = getUserPoints.rewardPoints
			//	rewards = existingPoints+rewards;
			//}
			//else{
			userRewardsPoints = new UserRewardPoints()
			//}
			userRewardsPoints.setVenue(order.venue)
			userRewardsPoints.setUser(order.user)
			userRewardsPoints.setOrder(order)
			userRewardsPoints.setRewardPoints(rewards)
			userRewardsPoints.save(flush:true)
		}
	}

	/*
	 * To parse and save menu format
	 */

	def menuParsingAndSave(def itemsList){
		itemsList.each {
			def itemDetails = it

			def itemName = itemDetails.itemName
			def title = itemDetails.title
			def name = itemDetails.name
			def options_description = itemDetails.options_description
			def type = itemDetails.type
			def order_price = itemDetails.order_price
			def basePrice = itemDetails.price
			def quantity =itemDetails.quantity


			def option_groups=itemDetails.option_groups
			String category
			String selectedItems
			if(option_groups && option_groups.size()>0){
				option_groups.each {
					def option = it
					def text = option.text
					if(text){
						def categoryObj = IngredientCategory.findByCategory(text.trim())
						if(category && !category.contains(categoryObj.id.toString())){
							category=category+","+categoryObj.id
						}
						else{
							category=categoryObj.id
						}
					}
					def options = option.options
					if(options && options.size()>0){
						options.each {
							def ingredient=it
							def selected= ingredient.selected
							if(selected){
								def ingredientName =ingredient.name
								if(selectedItems){
									selectedItems=selectedItems+","+ingredientName
								}
								else{
									selectedItems=ingredientName
								}
							}
						}
					}
				}
			}
			println"category "+category
			println"selectedItems "+selectedItems
			def usrFvtDrnk = new UserFavoriteDrinks()
			usrFvtDrnk.setItemsList(itemsList.toString())
			usrFvtDrnk.setItemName(itemName)
			usrFvtDrnk.setTitle(title)
			usrFvtDrnk.setBasePrice(basePrice)
			usrFvtDrnk.setName(name)
			usrFvtDrnk.setDescription(options_description)
			usrFvtDrnk.setQuantity(quantity)
			usrFvtDrnk.setQuantity(quantity)
			usrFvtDrnk.setType(type)
			usrFvtDrnk.setUser(user)
			usrFvtDrnk.setVenue(venue)
			usrFvtDrnk.setCategorys(category)
			usrFvtDrnk.setSelectedItems(selectedItems)
			println"END ********************"
			if(usrFvtDrnk.save(flush:true)) {
				return true
			}else{
				return false
			}

		}
	}

}
