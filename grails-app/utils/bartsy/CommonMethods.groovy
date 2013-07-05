package bartsy

import java.text.SimpleDateFormat

class CommonMethods {

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

}
