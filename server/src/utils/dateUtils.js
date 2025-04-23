const handleResponse = require('./responseUtils');

/** Return current time */
async function getLocationTime(ip) {
	// Fetch Loccation
	const locationRequest = await fetch(`http://ip-api.com/json/${ip}`);

	// Location Response
	const locationResponse = await handleResponse(locationRequest)
		.then((e) => {
			return {
				countryCode: e.countryCode,
				country: e.country,
				city: e.city,
			};
		})
		.catch(() => {
			return {
				countryCode: '##',
				country: 'Unknown',
				city: 'Unknown',
			};
		});

	return locationResponse;
}

module.exports = { getLocationTime };
