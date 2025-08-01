const CODE = 200; //EDIT THIS: Expected status code
const TEST_NAME = pm.info.requestName;

// --- Status Code Test ---
pm.test(`Status code is ${CODE}`, function () {
    pm.response.to.have.status(CODE);
});

// --- Define Expected Response ---
// EDIT THIS: Example expected structure; replace this with real data
const expectedResponse = {
    message: "Success",
    data: { id: 123 }
};

// --- Actual Response ---
const actualResponse = pm.response.json();

// --- Body Match Test ---
pm.test(`${TEST_NAME} response matches expected body`, function () {
    try {
        pm.expect(actualResponse).to.eql(expectedResponse);
        console.log("✅ ${TEST_NAME} success: Response matches expected");
        console.log("Response:\n", JSON.stringify(expectedResponse, null, 2));
    } catch (error) {
        console.log(`❌ ${TEST_NAME} failed: Response does not match expected value.`);
        console.log("Expected:\n", JSON.stringify(expectedResponse, null, 2));
        console.log("Actual:\n", JSON.stringify(actualResponse, null, 2));
        
        throw error;
    }
});
