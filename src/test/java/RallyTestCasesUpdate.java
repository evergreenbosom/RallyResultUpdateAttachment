import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

public class RallyTestCasesUpdate {
    static RallyRestApi restApi = null;
    static List<String> testSetRef;
    static String resultString = "";
    static String tcId = "";
    static String projectName = "Magenic Automation";


    //static String tcId = "TC8";
    static String testEnv = "Stage - ";
    public static void main(String[] args) throws Exception {

        // Connect to the Rally
        String baseURL = "https://rally1.rallydev.com";
        String apiKey = "_8ohhxYLRSTCUVXVPPeCS2iYdgxWx05DnfJHXZaTas";


        String resultDirName = "C:\\Users\\MohammodH\\OneDrive - Magenic\\Documents\\Result File\\";
        File folder = new File("data");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {

                //String resultFileName = resultDirName + "org.example.test.JMAQSSoapWebServiceDriverTest.SOAPDriverTestPass - 2020-11-23-18-21-25-7248.txt";
                String resultFileName = folder+"/"+file.getName();

                BufferedReader br = new BufferedReader(new FileReader(resultFileName));


                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Test Passed")) {
                        resultString = "Pass";
                    } else if (line.contains("Test Failed")) {
                        resultString = "Fail";
                    } else if (line.contains("Test Case ID:")) {
                        String[] tokens = line.split(":");
                        tcId = tokens[2].trim();
                    }
                }

                if (tcId.isEmpty()) {
                    throw new Exception("Did not find test case ID value in file " + resultFileName);
                }

                if (resultString.isEmpty()) {
                    throw new Exception("Did not find test result value in file " + resultFileName);
                }
                //  System.out.println(tcId);
                //  System.out.println(resultString);

                try {
                    restApi = new RallyRestApi(new URI(baseURL), apiKey); // "_2RFDlhDSQsS4MjJloQGUoD2BCZFUDZsxP7OQbrJno");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                restApi.setApplicationName("testSet Update");
                QueryResponse res = null;
                try {
                    res = restApi.query(new QueryRequest("workspace"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String workspaceRef = Ref.getRelativeRef(res.getResults().get(0).getAsJsonObject().get("_ref").getAsString());
                // String workspaceRef =
                // workReference.split("https://rally1.rallydev.com/slm/webservice/v2.0")[1];
                //System.out.println(workspaceRef);

                //String projectName = "Magenic Automation"; // Magenic
                String projectRef = Ref.getRelativeRef(getRef(workspaceRef, "Project", projectName).get("_ref").getAsString());
                // System.out.println("projectRef: "+projectRef);
                JsonObject currentIterationObj = getCurrentIterationRef(workspaceRef, projectRef);

                // System.out.println("::iternationName::"+currentIterationObj.get("Name").getAsString());


                // JsonObject projectObj = getRef(workspaceRef,"Project",projectName);
                //
//    			String tcId = Reporter.getCurrentTestResult().getAttribute("tcId").toString();
//    			String testEnv = Reporter.getCurrentTestResult().getAttribute("TestEnv").toString();
//
//    			String tcId = "TC8";
//    			String testEnv = "Dev - Iteration 3";


                //System.out.println(getCurrentIterationRef(workspaceRef, projectRef).get("Name"));
                // String currentIterationRef =
                // Ref.getRelativeRef(getCurrentIterationRef(workspaceRef,projectRef).get("_ref").getAsString());
                // String currentIterationName =
                // getCurrentIterationRef(workspaceRef,projectRef).get("Name").getAsString();
                List<String> allTestSet = getAllTestSet(projectRef, currentIterationObj);
                // if(allTestSet.isEmpty()) {System.out.println("it is empty");}

                //System.out.println(allTestSet);
                // System.out.println(":::::"+testSetRef);

                //System.out.println("starting call");

                // String tsRef = queryRequest(workspaceRef,
                // "TestSet",testSetRef.get(0).split("https://rally1.rallydev.com/slm/webservice/v2.0")[1]);//
                // JsonObject tsRef = getRef(workspaceRef, projectRef,"TestSet","Stage");
                JsonObject tsRef = getRef(workspaceRef, projectRef, "TestSet", testEnv + currentIterationObj.get("Name").getAsString());

                // if(tsRef==null) {System.out.println("null");}
                // System.out.println("::::"+tsRef.get("_ref").getAsString());
                JsonObject tcRef = queryRequest(workspaceRef, projectRef, "TestCase", tcId);// .get("_ref").getAsString();
                // if(tcRef==null) {System.out.println("null");}
                // System.out.println("::::"+tcRef.get("_ref").getAsString());

                addTSToTC(tsRef, tcRef);
                addTCResult(Ref.getRelativeRef(tsRef.get("_ref").getAsString()),
                        Ref.getRelativeRef(tcRef.get("_ref").getAsString()));

            }








        }
    }

    public static JsonObject getRef(String workspaceRef, String projectRef, String type, String name) {
        JsonObject ref = null;
        QueryRequest request = new QueryRequest(type);
        request.setFetch(
                new Fetch("Name", "ObjectID", "StartDate", "EndDate", "Project", "RevisionHistory", "TestSet"));
        request.setWorkspace(workspaceRef);
        request.setProject(projectRef);
        QueryResponse response = null;
        try {
            response = restApi.query(request);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // JsonObject jsonObject = response.getResults().get(0).getAsJsonObject();

        if (response.wasSuccessful()) {
            //	System.out.println(String.format("\nTotal results: %d", response.getTotalResultCount()));

            for (JsonElement result : response.getResults()) {
                // String releaseInit =
                // result.getAsJsonObject().get("_refObjectName").getAsString();
                String typeName = result.getAsJsonObject().get("Name").getAsString();
                // System.out.println(typeName);
                if (typeName.contains(name) && type.equals("TestSet")) {
                    ref = result.getAsJsonObject();// .get("_ref").getAsString();
                    // System.out.println(result.getAsJsonObject().get("_ref").getAsString());
                    break;
                }

                if (typeName.equals(name)) {
                    ref = result.getAsJsonObject();// .get("_ref").getAsString();
                    // System.out.println(result.getAsJsonObject().get("_ref").getAsString());
                    break;
                }

            }
        }
        return ref;
    }

    public static JsonObject getRef(String workspaceRef, String type, String name) {
        JsonObject ref = null;
        QueryRequest request = new QueryRequest(type);
        request.setFetch(
                new Fetch("Name", "ObjectID", "StartDate", "EndDate", "Project", "RevisionHistory", "TestSet"));
        request.setWorkspace(workspaceRef);
        // request.setProject("/project/"+projectRef);
        request.setLimit(2);
        // JsonObject currentIterationObj =
        // getCurrentIterationRef(workspaceRef,projectRef);

        // request.setQueryFilter(new QueryFilter("Iteration.Name", " = ",
        // currentIterationObj.get("Name").getAsString()));

        QueryResponse response = null;
        try {
            response = restApi.query(request);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // JsonObject jsonObject = response.getResults().get(0).getAsJsonObject();

        if (response.wasSuccessful()) {
            //System.out.println(String.format("\nTotal results: %d", response.getTotalResultCount()));

            for (JsonElement result : response.getResults()) {
                // String releaseInit =result.getAsJsonObject().get("_refObjectName").getAsString();
                String typeName = result.getAsJsonObject().get("Name").getAsString();
                // System.out.println(typeName);
                if (typeName.contains(name) && type.equals("TestSet")) {
                    ref = result.getAsJsonObject();// .get("_ref").getAsString();
                    // System.out.println(result.getAsJsonObject().get("_ref").getAsString());
                    break;
                }

                if (typeName.equals(name)) {
                    ref = result.getAsJsonObject();// .get("_ref").getAsString();
                    // System.out.println(result.getAsJsonObject().get("_ref").getAsString());
                    break;
                }

            }
        }
        return ref;
    }

    //
    public static JsonObject getCurrentIterationRef(String workspaceRef, String projectRef) {
        QueryRequest currentIterationRequest = new QueryRequest("Iteration");
        currentIterationRequest.setFetch(new Fetch("FormattedID", "Name", "StartDate", "EndDate"));

        String pattern = "yyyy-MM-dd'T'HH:mmZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(new Date());
        // System.out.println(date);

        currentIterationRequest
                .setQueryFilter(new QueryFilter("StartDate", "<=", date).and(new QueryFilter("EndDate", ">=", date)));

        currentIterationRequest.setWorkspace(workspaceRef);
        currentIterationRequest.setProject(projectRef);

        QueryResponse response = null;
        try {
            response = restApi.query(currentIterationRequest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JsonObject iterationRef = response.getResults().get(0).getAsJsonObject();// .get("_ref").getAsString();
        return iterationRef;
    }

    public static List<String> getAllTestSet(String projectRef, JsonObject currentIterationObj) {

        List<String> testSetList = new ArrayList<String>();
        testSetRef = new ArrayList<String>();
        QueryRequest testSetRequest = new QueryRequest("TestSet");
        testSetRequest.setFetch(new Fetch("FormattedID", "Name"));
        testSetRequest.setQueryFilter(
                new QueryFilter("Iteration.Name", " = ", currentIterationObj.get("Name").getAsString()));
        // testSetRequest.setWorkspace(workspaceRef);
        testSetRequest.setProject(projectRef);

        QueryResponse response = null;
        try {
            response = restApi.query(testSetRequest);
            // System.out.println("total count:::"+response.getTotalResultCount());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (response.wasSuccessful()) {
//			System.out.println(String.format("\nTotal results: %d", response.getTotalResultCount()));

            if (response.getTotalResultCount() == 0) {
                addTestSet(projectRef, Ref.getRelativeRef(currentIterationObj.get("_ref").getAsString()),
                        "Dev - "+currentIterationObj.get("Name").getAsString());
                testSetList.add("Dev");

                addTestSet(projectRef, Ref.getRelativeRef(currentIterationObj.get("_ref").getAsString()),
                        "Stage - "+currentIterationObj.get("Name").getAsString());
                testSetList.add("Stage");

            }

            for (JsonElement result : response.getResults()) {

                String testSetName = result.getAsJsonObject().get("Name").getAsString();
                // System.out.println(testSetName);
                if (testSetName.equalsIgnoreCase("Dev - "+currentIterationObj.get("Name").getAsString())) {
                    testSetList.add("Dev");
                    testSetRef.add(Ref.getRelativeRef(result.getAsJsonObject().get("_ref").getAsString()));
                } else if (testSetName.equalsIgnoreCase("Stage - "+currentIterationObj.get("Name").getAsString())) {
                    testSetList.add("Stage");
                    testSetRef.add(Ref.getRelativeRef(result.getAsJsonObject().get("_ref").getAsString()));

                }
            }
            if (testSetList.isEmpty()) {
                addTestSet(projectRef, Ref.getRelativeRef(currentIterationObj.get("_ref").getAsString()),
                        "Dev - "+currentIterationObj.get("Name").getAsString());
                testSetList.add("Dev");

                addTestSet(projectRef, Ref.getRelativeRef(currentIterationObj.get("_ref").getAsString()),
                        "Stage - "+currentIterationObj.get("Name").getAsString());
                testSetList.add("Stage");
            }

            //System.out.println(":::::" + testSetRef);
            // System.out.println(releaseInit);

        }
        return testSetList;
    }

    // type: Iteration,TestCase, TestSet,
    public static JsonObject queryRequest(String workspaceRef, String projectRef, String type, String id) {
        QueryRequest request = new QueryRequest(type);
        request.setFetch(new Fetch("FormattedID", "Name", "TestSets"));
        request.setWorkspace(workspaceRef);
        request.setProject(projectRef);
        request.setQueryFilter(new QueryFilter("FormattedID", "=", id));
        QueryResponse response = null;
        try {
            response = restApi.query(request);
            if (response.getTotalResultCount() == 0) {
                System.out.println("Cannot find tag: " + id);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject jsonObject = response.getResults().get(0).getAsJsonObject();
        return jsonObject;
    }

    public static void addTestSet(String projectRef, String iterationIdRef, String testSetName) {

        JsonObject newTS = new JsonObject();
        newTS.addProperty("Project", projectRef);
        newTS.addProperty("Name", testSetName);
        // newTS.addProperty("Project", "/project/81223493876");
        // newTS.addProperty("Tags", tagName);
        // newTS.addProperty("Release.Name", releaseID);
        newTS.addProperty("Iteration", iterationIdRef);
        // newTS.addProperty("projectScopeUp", false);
        // newTS.addProperty("projectScopeDown", true);
        newTS.addProperty("fetch", true);
        // newTS.addProperty("rankTo", "BOTTOM");
        // newTS.add("TestCases", testCaseList);

        CreateRequest createRequest = new CreateRequest("testset", newTS);
        try {
            CreateResponse createResponse = restApi.create(createRequest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // add test case to test set
    public static void addTSToTC(JsonObject tsJsonObject, JsonObject testCaseJsonObject) {

        String tsRef = tsJsonObject.get("_ref").getAsString();
        //System.out.println(tsRef);

        String testCaseRef = testCaseJsonObject.get("_ref").getAsString();
        //System.out.println(testCaseRef);

        int numberOfTestSets = testCaseJsonObject.getAsJsonObject("TestSets").get("Count").getAsInt();
        // System.out.println(numberOfTestSets + " testset(s) on " + tcId);

        QueryRequest testsetCollectionRequest = new QueryRequest(testCaseJsonObject.getAsJsonObject("TestSets"));
        testsetCollectionRequest.setFetch(new Fetch("FormattedID"));
        JsonArray testsets = null;
        try {
            testsets = restApi.query(testsetCollectionRequest).getResults();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // for (int j=0;j<numberOfTestSets;j++){
        // System.out.println("FormattedID: " +
        // testsets.get(j).getAsJsonObject().get("FormattedID"));
        // }
        testsets.add(tsJsonObject);

        JsonObject testCaseUpdate = new JsonObject();
        testCaseUpdate.add("TestSets", testsets);
        UpdateRequest updateTestCaseRequest = new UpdateRequest(testCaseRef, testCaseUpdate);
        UpdateResponse updateTestCaseResponse = null;
        try {
            updateTestCaseResponse = restApi.update(updateTestCaseRequest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (updateTestCaseResponse.wasSuccessful()) {
            QueryRequest testsetCollectionRequest2 = new QueryRequest(testCaseJsonObject.getAsJsonObject("TestSets"));
            testsetCollectionRequest2.setFetch(new Fetch("FormattedID"));
            try {
                JsonArray testsetsAfterUpdate = restApi.query(testsetCollectionRequest2).getResults();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int numberOfTestSetsAfterUpdate = 0;
            try {
                numberOfTestSetsAfterUpdate = restApi.query(testsetCollectionRequest2).getResults().size();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // System.out.println("Successfully updated : " + tcId + " TestSets after
            // update: " + numberOfTestSetsAfterUpdate);
            for (int j = 0; j < numberOfTestSetsAfterUpdate; j++) {
                // System.out.println("FormattedID: " +
                // testsetsAfterUpdate.get(j).getAsJsonObject().get("FormattedID"));
            }
        }

    }

    // Add a Test Case Result in test set
    public static void addTCResult(String tsRef, String tcRef) {
        JsonObject newTestCaseResult = new JsonObject();
        //resultString need to update
        newTestCaseResult.addProperty("Verdict", resultString);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String timestamp = sdf.format(date);
        newTestCaseResult.addProperty("Date", timestamp);
        newTestCaseResult.addProperty("Build", "Build number# " + timestamp);
        newTestCaseResult.addProperty("Description", "Automaed test case updated.");
        newTestCaseResult.addProperty("TestCase", tcRef);

        newTestCaseResult.addProperty("TestSet", tsRef);

        CreateRequest createRequest = new CreateRequest("testcaseresult", newTestCaseResult);
        try {
            CreateResponse createResponse = restApi.create(createRequest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            restApi.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
