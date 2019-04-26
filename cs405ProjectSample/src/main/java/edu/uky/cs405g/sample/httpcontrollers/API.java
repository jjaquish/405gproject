package edu.uky.cs405g.sample.httpcontrollers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uky.cs405g.sample.Launcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.sql.Timestamp;
import java.util.Date;

@Path("/api")
public class API {

    private Type mapType;
    private Gson gson;

    public API() {
        mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        gson = new Gson();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {

        String responseString = "{\"status_code\":0}";
        try {
            responseString = "{\"status_code\":1}";

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //--------------------------------------------------------------------------//
    //---------------------------Start Location Block---------------------------//
    //--------------------------------------------------------------------------//

    @POST
    @Path("/addlocation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addLocation(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String address = myMap.get("address");

            Map<String,String> addressMap = Launcher.dbEngine.getLocation(address);

            if(addressMap.size() == 0) {

                //generate a new unique location Id
                String locationId = UUID.randomUUID().toString();

                String createUsersTable = "insert into location values ('" + locationId + "','" + address  + "')";

                System.out.println(createUsersTable);

                Launcher.dbEngine.executeUpdate(createUsersTable);

                addressMap = Launcher.dbEngine.getLocation(address);

                returnString = gson.toJson(addressMap);


            } else {
                return Response.status(Response.Status.FORBIDDEN).entity("Can't insert duplicate location address!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/listlocations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listLocations() {
        String responseString = "{}";
        try {
            Map<String,String> teamMap = Launcher.dbEngine.getLocations();

            responseString = Launcher.gson.toJson(teamMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getlocation/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocation(@PathParam("address") String address) {
        String responseString = "{}";
        try {

            Map<String,String> teamMap = Launcher.dbEngine.getLocation(address);

            responseString = Launcher.gson.toJson(teamMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removelocation/{location_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLocation(@PathParam("location_id") String locationId) {
        String responseString = "{}";
        try {


            String queryString = "delete from location WHERE lid='" + locationId + "'";

            System.out.println(queryString);

            int status = Launcher.dbEngine.executeUpdate(queryString);

            System.out.println("status_code: " + status);

            responseString = "{\"status_code\":\"" + status +"\"}";

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the location. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the location. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //--------------------------------------------------------------------------//
    //----------------------------End Location Block----------------------------//
    //--------------------------------------------------------------------------//


    //--------------------------------------------------------------------------//
    //---------------------------Start Service Block----------------------------//
    //--------------------------------------------------------------------------//

    @POST
    @Path("/addservice")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crunchifyREST12(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            String dpInstId = "";
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);

            String address = myMap.get("address");
            String departmentId = myMap.get("department_id");
            String serviceId = myMap.get("service_id");
            String taxId = myMap.get("taxid");

            Map<String,String> addressMap = Launcher.dbEngine.getLocation(address);
            Map<String, String> departmentMap = Launcher.dbEngine.getDepartment(departmentId);
            Map<String, String> serviceMap = Launcher.dbEngine.getService(serviceId);
            Map<String, String> taxMap = Launcher.dbEngine.getInstitution(taxId);

            
            //Everything depends on the service ID, because this is the only one that must be new.
            if(serviceMap.size() == 0) {

                //this means the location doesn't exist
                //and there isn't anything to worry about here.
                if (addressMap.size() == 0) {

                    //generate a new unique location ID
                    String locationId = UUID.randomUUID().toString();

                    String createLocationTable = "insert into location values ('" + locationId + "','" + address  + "')";

                    System.out.println(createLocationTable);

                    Launcher.dbEngine.executeUpdate(createLocationTable);

                    addressMap = Launcher.dbEngine.getLocation(address);
                }

                //There is some stuff to worry about with department.
                if (departmentMap.size() == 0) {

                    //The department doesn't exist in here.
                    //We want to know if the institution exists before
                    //we waste time making it.

                    if (taxMap.size() == 0) {
                        //The department doesn't exist and the institution doesn't exist in here.
                        //We have to have a valid institution ID before we make the department

                        //We generate a new institution ID
                        //But we already haver a valid taxId
                        dpInstId = UUID.randomUUID().toString();

                        String createInstitutionTable = "insert into institution values ('" + dpInstId +"', '" + taxId + "')";

                        System.out.println(createInstitutionTable);

                        Launcher.dbEngine.executeUpdate(createInstitutionTable);

                        taxMap = Launcher.dbEngine.getInstitution(taxId);

                    } else {
                        //The department doesn't exist but the institution does exist in here.
                        dpInstId = taxMap.get("id");
                    }

                    //Once we've gotten here, we should have a valid institution ID
                    //regardless of what else happened.


                    //At this point we can create the department entry
                    String createDepartmentTable = "insert into department values ('" + departmentId + "', '" + dpInstId + "')";

                    System.out.println(createDepartmentTable);

                    Launcher.dbEngine.executeUpdate(createDepartmentTable);

                    departmentMap = Launcher.dbEngine.getDepartment(departmentId);

                } else {
                //This means the department did exist
                //all we want to do in here is make sure that the taxID they
                //entered matches what is in the database for the department.
                    //Here is where we throw the error if the taxId doesn't match
                    String mytid = taxMap.get("id");
                    String myiid = departmentMap.get("institution_id");

                    if (taxMap.size() == 0) { // || taxMap.get("id") != departmentMap.get("institution_id")) {

                        return Response.status(Response.Status.FORBIDDEN).entity("The taxId you entered doesn't exist even though the department does!")
                        .header("Access-Control-Allow-Origin", "*").build();
                    } else if(!mytid.equals(myiid)) {
                        System.out.println(mytid + "\n" + myiid);
                       return Response.status(Response.Status.FORBIDDEN).entity("The taxId you entered doesn't match the pre-existing department!")
                        .header("Access-Control-Allow-Origin", "*").build(); 
                    }

                }

                //At this point departmentMap is ready to go
                //and taxMap is ready to go

                String createServiceTable = "insert into service values('" + serviceId + "', '" + addressMap.get("lid") + "', '" + departmentId + "')";

                Launcher.dbEngine.executeUpdate(createServiceTable);

                serviceMap = Launcher.dbEngine.getService(serviceId);

                returnString = "{\"status_code\":1}";


            } else {
                return Response.status(Response.Status.FORBIDDEN).entity("Can't insert duplicate service ID!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getservice/{service_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getService(@PathParam("service_id") String service_id) {
        String responseString = "{}";
        try {

            Map<String,String> serviceMap = Launcher.dbEngine.getService(service_id);
            //Step 1 is to get the service
            if (serviceMap.size() != 0) {
                //Then we need a few things that aren't directly in service,
                //so we grab those with the data from the service.
                Map<String, String> locationMap = Launcher.dbEngine.getLocationFromId(serviceMap.get("location_id"));
                Map<String, String> departmentMap = Launcher.dbEngine.getDepartment(serviceMap.get("department_id"));
                Map<String, String> instMap = Launcher.dbEngine.getInstitutionFromId(departmentMap.get("institution_id"));
                //This is a new mao we're creating for the results because no method
                //in DBEngine returns what we need exactly
                Map<String, String> responseMap = new HashMap<>();
                
                //Here we put the various parts of our response
                //into the map we created earlier for it.
                responseMap.put("address", locationMap.get("address"));
                responseMap.put("department_id", serviceMap.get("department_id"));
                responseMap.put("taxid", instMap.get("taxid"));
                responseMap.put("service_id", serviceMap.get("id"));

                responseString = Launcher.gson.toJson(responseMap);
            }

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removeservice/{service_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteService(@PathParam("service_id") String service_id) {
        String responseString = "{}";
        try {

            //First step is to verify that the service exists
            //And grab data from it for the cleanup
            Map<String, String> serviceMap = Launcher.dbEngine.getService(service_id);
            int status;

            if (serviceMap.size() != 0) {
                String queryString = "delete from service WHERE id='" + service_id + "'"; 
                status = Launcher.dbEngine.executeUpdate(queryString);
            } else {
                status = 0;
            }


            responseString = "{\"status_code\":\"" + status +"\"}";

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the service. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the service. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //stuff for PLAN B

    @GET
    @Path("/removedepartment/{department_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDepartment(@PathParam("department_id") String department_id) {
        String responseString = "{}";
        try {

            Map<String, String> departmentMap = Launcher.dbEngine.getDepartment(department_id);

            int status;


            if (departmentMap.size() != 0) {
                

                String queryString = "delete from department WHERE id='" + department_id + "'"; 
                
                status = Launcher.dbEngine.executeUpdate(queryString);
                //Handle other stuff
            } else {
                status = 0;
            }


            responseString = "{\"status_code\":\"" + status +"\"}";

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the department. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the department. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removeinstitution/{taxid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInstitution(@PathParam("taxid") String taxid) {
        String responseString = "{}";
        try {

            Map<String, String> institutionMap = Launcher.dbEngine.getInstitution(taxid);

            int status;


            if (institutionMap.size() != 0) {
                

                String queryString = "delete from institution WHERE tid='" + taxid + "'"; 
                
                status = Launcher.dbEngine.executeUpdate(queryString);
                //Handle other stuff
            } else {
                status = 0;
            }


            responseString = "{\"status_code\":\"" + status +"\"}";

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the institution. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the institution. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removeaddress/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLocationByAddress(@PathParam("address") String address) {
        String responseString = "{}";
        try {

            Map<String, String> locationMap = Launcher.dbEngine.getLocation(address);

            int status;


            if (locationMap.size() != 0) {
                

                String queryString = "delete from location WHERE address='" + address + "'"; 
                
                status = Launcher.dbEngine.executeUpdate(queryString);
                //Handle other stuff
            } else {
                status = 0;
            }

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the location. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the location. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }


            responseString = "{\"status_code\":\"" + status +"\"}";


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }


    //--------------------------------------------------------------------------//
    //-----------------------------End Service Block----------------------------//
    //--------------------------------------------------------------------------//


    //--------------------------------------------------------------------------//
    //--------------------------Start Provider Block----------------------------//
    //--------------------------------------------------------------------------//


    @POST
    @Path("/addprovider")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crunchifyREST13(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = "{\"status_code\":\"" + '0' +"\"}";
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String npi = myMap.get("npi");
            String department_id = myMap.get("department_id");

            Map<String,String> departmentMap = Launcher.dbEngine.getDepartment(department_id);
            System.out.println(departmentMap.size());

            if(departmentMap.size() != 0) {
                Map<String, String> providerMap = Launcher.dbEngine.getProvider(npi);

                if (providerMap.size() == 0) {
                    String createUsersTable = "insert into provider values ('" + npi + "','" + department_id  + "')";
                    System.out.println(createUsersTable);
                    int status = Launcher.dbEngine.executeUpdate(createUsersTable);
                    System.out.println("status_code: " + status);
                    returnString = "{\"status_code\":\"" + status +"\"}";

                } else {
                    return Response.status(Response.Status.FORBIDDEN).entity("The provided npi already exists!")
                    .header("Access-Control-Allow-Origin", "*").build();
                }
                

            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("The provided department_id doesn't exist!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getprovider/{npi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProv(@PathParam("npi") String npi) {
        String responseString = "{}";
        try {

            Map<String,String> providerMap = Launcher.dbEngine.getProvider(npi);

            responseString = Launcher.gson.toJson(providerMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removeprovider/{npi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProvider(@PathParam("npi") String npi) {
        String responseString = "{}";
        try {


            String queryString = "delete from provider WHERE npi='" + npi + "'";

            System.out.println(queryString);

            int status = Launcher.dbEngine.executeUpdate(queryString);

            System.out.println("status_code: " + status);

            responseString = "{\"status_code\":\"" + status +"\"}";

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the provider. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the provider. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //--------------------------------------------------------------------------//
    //---------------------------End Provider Block-----------------------------//
    //--------------------------------------------------------------------------//


    //--------------------------------------------------------------------------//
    //---------------------------Start Patient Block----------------------------//
    //--------------------------------------------------------------------------//

    @POST
    @Path("/addpatient")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPatient(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        int status = 0;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String pid = myMap.get("pid");
            String ssn = myMap.get("ssn");
            //
            String address = myMap.get("address");
            //provider id must exist already
            String provider_id = myMap.get("provider_id");

            //Map<String,String> addressMap = Launcher.dbEngine.getPatientByAddress(address);
            Map<String,String> patientMap = Launcher.dbEngine.getPatient(pid);
            Map<String,String> providerMap = Launcher.dbEngine.getProvider(provider_id);
            Map<String, String> ssnMap = Launcher.dbEngine.getPatientBySSN(ssn);

            if(patientMap.size() == 0) {
                if (ssnMap.size() == 0) {

                    if(providerMap.size() == 0) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Invalid Provider ID entered!")
                        .header("Access-Control-Allow-Origin", "*").build();
                    }
                
                    /*if(addressMap.size() == 0) {

                        //generate a new unique location Id
                        String locationId = UUID.randomUUID().toString();

                        String createUsersTable = "insert into location values ('" + locationId + "','" + address  + "')";

                        System.out.println(createUsersTable);

                        Launcher.dbEngine.executeUpdate(createUsersTable);

                        addressMap = Launcher.dbEngine.getLocation(address);


                    } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't insert duplicate location address!")
                                .header("Access-Control-Allow-Origin", "*").build();
                    }*/

                    String createUsersTable = "insert into patient value ('" + pid + "','" + ssn + "', '" + address + "','" + provider_id + "')";
                    System.out.println(createUsersTable);
                    status = Launcher.dbEngine.executeUpdate(createUsersTable);
                    patientMap = Launcher.dbEngine.getPatient(pid);
                    returnString = "{\"status_code\":\"" + status +"\"}";
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Cannot Insert duplicate social security numbers!")
                    .header("Access-Control-Allow-Origin", "*").build();
                }

            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Cannot Insert duplicate patient ID numbers!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getpatient/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatient(@PathParam("pid") String pid) {
        String responseString = "{}";
        try {

            Map<String,String> patientMap = Launcher.dbEngine.getPatient(pid);

            responseString = Launcher.gson.toJson(patientMap);
            //404 would be not found

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removepatient/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePatient(@PathParam("pid") String pid) {
        String responseString = "{}";
        try {

            String queryString = "delete from patient WHERE pid='" + pid + "'";

            System.out.println(queryString);

            int status = Launcher.dbEngine.executeUpdate(queryString);

            System.out.println("status_code: " + status);

            responseString = "{\"status_code\":\"" + status +"\"}";

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the patient. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the patient. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //--------------------------------------------------------------------------//
    //----------------------------End Patient Block-----------------------------//
    //--------------------------------------------------------------------------//


    //--------------------------------------------------------------------------//
    //-----------------------------Start Data Block-----------------------------//
    //--------------------------------------------------------------------------//

    @POST
    @Path("/adddata")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adddata(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);

            String id = myMap.get("id");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String patient_id = myMap.get("patient_id");
            String service_id = myMap.get("service_id");
            String some_data = myMap.get("data");

            Map<String,String> patientMap = Launcher.dbEngine.getPatient(patient_id);
            System.out.println(patientMap.size());
            Map<String,String> serviceMap = Launcher.dbEngine.getService(service_id);
            System.out.println(serviceMap.size());
            Map<String, String> dataMap = Launcher.dbEngine.getData(id);
            System.out.println(dataMap.size());

            if (dataMap.size() != 0) {
                return Response.status(Response.Status.FORBIDDEN).entity("The provided data id already exists!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }

            if(patientMap.size() != 0) {

                if(serviceMap.size() != 0) {

                    dataMap = Launcher.dbEngine.getData(id);

                    if (dataMap.size() == 0) {
                        //we're in business
                        String createUsersTable = "insert into data values ('" + id + "','" + timestamp.toString() + "','" + patient_id + "','" + service_id + "','" + some_data + "')";

                        System.out.println(createUsersTable);

                        int status = Launcher.dbEngine.executeUpdate(createUsersTable);

                        System.out.println("status_code: " + status);

                        returnString = "{\"status_code\":\"" + status +"\"}";

                    } else {
                        return Response.status(Response.Status.FORBIDDEN).entity("The provided id already exists!")
                        .header("Access-Control-Allow-Origin", "*").build();
                    }
                    
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity("The provided service_id doesn't exist!")
                            .header("Access-Control-Allow-Origin", "*").build();
                }

            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("The provided patient_id doesn't exist!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getdata/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getData(@PathParam("id") String id) {
        String responseString = "{}";
        try {

            Map<String,String> dataMap = Launcher.dbEngine.getData(id);
            responseString = Launcher.gson.toJson(dataMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removedata/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteData(@PathParam("id") String id) {
        String responseString = "{}";
        try {

            String queryString = "delete from data WHERE id='" + id + "'";

            System.out.println(queryString);

            int status = Launcher.dbEngine.executeUpdate(queryString);

            System.out.println("status_code: " + status);

            responseString = "{\"status_code\":\"" + status +"\"}";

            if (status == -1) {
                return Response.status(Response.Status.FORBIDDEN).entity("Couldn't remove the data. It is referenced elsewhere!")
                    .header("Access-Control-Allow-Origin", "*").build();
            } else if (status == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Couldn't remove the data. It doesn't exist!")
                    .header("Access-Control-Allow-Origin", "*").build();
            }



        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }


    //--------------------------------------------------------------------------//
    //------------------------------End Data Block------------------------------//
    //--------------------------------------------------------------------------//

}
