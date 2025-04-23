/**
 * Handle HTTPS request response
 *
 * This function processes the response object from a fetch request,
 * parsing it based on the content type and handling errors appropriately.
 *
 * @param {Response} requestResponse - The response object from a fetch request.
 * @returns {Promise<any>} A promise that resolves to the parsed JSON response
 *                        or rejects with an error.
 */
async function handleResponse(requestResponse) {
	// Check if the input is a valid Response object
	if (!(requestResponse instanceof Response)) {
		throw new Error('Invalid argument: requestResponse must be an instance of Response');
	}

	// Get the content type from the response headers
	const contentType = requestResponse.headers.get('content-type');

	// Check if the response status indicates success
	if (requestResponse.ok) {
		// If the response is OK, parse based on content type
		if (contentType?.includes('application/json')) {
			try {
				// Attempt to parse the JSON payload from the response
				const response = await requestResponse.json();

				// Return the parsed JSON response
				return response;
			} catch (error) {
				// Throw an error if JSON parsing fails
				throw new Error(`Error parsing JSON: ${error.message}`);
			}
		} else {
			// Handle non-JSON response types (e.g., text or HTML)
			const text = await requestResponse.text();
			return text; // Return the plain text response
		}
	} else {
		// Handle error responses (non-2xx status codes)

		// Read the error message from the response body
		let errorData;

		// Attempt to parse the error response as JSON
		try {
			errorData = await requestResponse.json();
		} catch (error) {
			// If parsing fails, set a fallback error data message
			errorData = { message: 'Failed to parse error response as JSON' };
		}

		// Read the error message from the response body
		const errorMessage = await requestResponse.text();

		// Create a new error object with the error message
		const error = new Error(errorMessage);

		// Indicate that this is a request error
		error.isRequestError = true;

		// Indicate the error code
		error.code = requestResponse.status;

		// Attach additional error information, including status and data
		error.response = {
			status: requestResponse.status,
			data: errorData,
		};

		throw error;
	}
}

module.exports = handleResponse;
