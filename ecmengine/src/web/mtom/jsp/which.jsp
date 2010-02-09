<%@ page import="java.util.*,java.io.*,java.net.*" %>
<%@ page import="javax.naming.*" %>
<%@ page import="javax.ejb.*" %>

<%!
   // Utility functions.

   String separator = System.getProperty("path.separator");
   StringTokenizer tokenizer;

   public String makeHREF(String filename)
   {
      StringBuffer buf = new StringBuffer();
      File file = new File(filename);

      buf.append("<tr>");
      if (!file.exists()) {
         buf.append("<td class=\"listContentNF\">");
          buf.append(filename);
          buf.append(" not found");
      } else {
         buf.append("<td class=\"listContent\">");
         buf.append("<a href=\"");
         buf.append(filename);
         buf.append("\">");
         buf.append(filename);
         buf.append("</a>");
      }
      buf.append("</td></tr>");

      return buf.toString();
   }

   public String getJars(String directoryName) 
   {
      File directory = new File(directoryName);

      if (!directory.exists()) {
          return null;
      } else {
          StringBuffer buf = new StringBuffer();
         String[] allFiles = directory.list();
         if (allFiles != null) {
            for (int i = 0; i < allFiles.length; i++) {
               if (allFiles[i].endsWith(".jar")) {
                  File f = new File(directory, allFiles[i]);
                  buf.append(makeHREF(f.getPath()));
               }
            }
         }
          return buf.toString();
      }
   }


   
   
%>

<html>

<head>
<title>Classloading Tester</title>
</head>

<style>
a              { text-decoration: underline; color: #336699; }
a:hover        { text-decoration: none; color: #cc0000; }
td             { font-family: verdana, arial,helvetica; color: #333333; font-size: 10pt; }
.pageTitle     { color: #FFFFFF; font-size: 14pt; line-height: 20pt; text-align: center; background-color: #000000; }
.sectionTitle  { color: #FFFFFF; font-size: 10pt; line-height: 15pt; font-weight: bold; padding-left: 10px; background-color: #000000; }
.listContent   { padding-left: 10px; padding-right: 10px; vertical-align: top; background-color: #EEEEEE; }
.listContentNF { padding-left: 10px; vertical-align: top; background-color: #EEEEEE; font-style: italic; }
.errMsg        { padding-left: 10px; padding-right: 10px; text-align: center; vertical-align: top; background-color: #EEEEEE; font-style: italic; color: #CC3333; font-weight: bold; }
</style>

<body bgcolor="#FFFFFF" text="#333333" topmargin="0" leftmargin="0" marginheight="0" marginwidth="0">

<!-- center -->
<br>
<table cellpadding="0" cellspacing="2" border="0" width="80%" bgcolor="#003366">
  <tr>
    <td class="pageTitle">Classloading Tester</td>
  </tr>
  <tr>
    <td bgcolor="#FFFFFF">
      <table cellpadding="0" cellspacing="0" width="100%" border="0">
        <tr>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align="center">
            <form action="which.jsp" method="POST">
              <table cellpadding="2" cellspacing="1" border="0" bgcolor="#003366">
                <tr>
                  <td bgcolor="#FFFFFF">
                    <table cellpadding="1" cellspacing="0" border="0">
                      <tr>
                        <td><b>Class to load:</b></td>
                        <td><input type="text" name="classname"></td>
                        <td><INPUT TYPE="submit" VALUE="Submit"></td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </form>
            </td>
        </tr>

<%  
    String classname = request.getParameter("classname");

    if (classname != null && classname.length() > 0) {
      try {  
      
        String s=classname;
        if(!s.startsWith("/"))
            s = "/" + s;
        s = s.replace('.', '/');
        s = s + ".class";
        URL myUrl = this.getClass().getResource(s);
        String s1 = null;
        if(myUrl != null)
            s1 = "\nClass '" + s + "' found in \n'" + myUrl.getFile() + "'";
        else
            s1 = "\nClass '" + s + "' not found.'";
     
           

    
          
%>
        <tr>
          <td align="center">
            <table cellpadding="2" cellspacing="1" border="0" width="95%">
              <tr>
                <td colspan="2" class="sectionTitle">
                
              
                        &nbsp <%= s %>
                </td>
              </tr>

<%

            boolean firstTime = true;

            if (!firstTime) {
                out.println("<tr><td class=\"listContent\">&nbsp;</td>");
            }


            String resourceName = classname;

            if (!resourceName.startsWith("/")) {
                resourceName = "/" + resourceName;
            }
            resourceName = resourceName.replace('.', '/');
            resourceName = resourceName + ".class";

            URL url = this.getClass().getResource(resourceName); 
            if (url != null) {
                out.println("<tr>");
                out.println("<td class=\"listContent\">Class found in file: </td>");
                out.println("<td class=\"listContent\">" + url.getFile() + "</td></tr>");
            }
            else {
            
                out.println("<tr>");
                out.println("<td class=\"listContent\">Class not found.</td></tr>");
            
            }

            out.println("</table>");
}
 catch (Exception e) {
          out.println("<tr><td class=\"listContentNF\">");
          out.println("Exception loading class: "+ classname);
          out.println("");  
          e.printStackTrace(new PrintWriter(out));
          out.println("</td></tr>");
        } 

        out.println("</td>");
        out.println("</tr>");
        out.println("<tr><td>&nbsp;</td></tr>");
    }
%>
          <tr>
            <td align="center">
              <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                  <td colspan=2 class="sectionTitle">Classloaders for this JSP</td>
                </tr>        

              <tr>
                <td class="listContent">
                  ClassLoader: 
                </td>
                <td class="listContent">
                  <%= this.getClass().getClassLoader().getClass().getName() %>
                </td>
              </tr>
<%
            ClassLoader loader = this.getClass().getClassLoader().getParent();
            boolean firstTime = true;

            out.println("<tr><td class=\"listContent\">Parent(s):</td>");
            while (loader != null) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    out.println("<tr><td class=\"listContent\">&nbsp;</td>");
                }
                out.println("<td class=\"listContent\">");
                out.println(loader.getClass().getName());
                out.println("</td></tr>");
                loader = loader.getParent();
            }
            if (!firstTime) {
                out.println("<tr><td class=\"listContent\">&nbsp;</td>");
            }
            out.println("<td class=\"listContent\">Bootstrap classloader</td></tr>");
%>
              </table>
            </td>
          </tr>

        <tr>
            <td align="center">
              <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                  <td class="sectionTitle">Boot Classes <i>(from sun.boot.class.path)</i></td>
                </tr>        

<%
   String bootClasspath = System.getProperty("sun.boot.class.path");
   tokenizer = new StringTokenizer(bootClasspath, separator);
   while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      out.println(makeHREF(token));
   }
%>
              </table>
            </td>
          </tr>
          
          

          <tr>
            <td align="center">
            
              <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                  <td class="sectionTitle">Endorsed Classes <i>(from java.endorsed.dirs)</i></td>
                </tr>        
<%

   String endorsedClasspath = System.getProperty("java.endorsed.dirs");
   if (endorsedClasspath == null) {
   	endorsedClasspath ="";
   }
   tokenizer = new StringTokenizer(endorsedClasspath, separator);
   while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      out.println(getJars(token));
   }
%>
              </table>
              
            </td>
          </tr>




          
          
          

          <tr>
            <td align="center">
            
              <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                  <td class="sectionTitle">Extension Classes <i>(from java.ext.dirs)</i></td>
                </tr>        
<%

   String extensionClasspath = System.getProperty("java.ext.dirs");
   tokenizer = new StringTokenizer(extensionClasspath, separator);
   while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      out.println(getJars(token));
   }
%>
              </table>
              
            </td>
          </tr>



          <tr>
            <td align="center">
              <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                  <td class="sectionTitle">Application Classes <i>(from java.class.path)</i></td>
                </tr>        
<%
   // Note that relative paths are displayed as HREFs in the UI, but
   // don't resolve correctly (if clicked will give 404 error). A basepath
   // variable or some other mechanism needed to make that work.

   String systemClasspath = System.getProperty("java.class.path");
   tokenizer = new StringTokenizer(systemClasspath, separator);
   while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      out.println(makeHREF(token));
   }
%>
              </table>
            </td>
          </tr>

          <tr>
            <td align="center">
              <table cellpadding="2" cellspacing="1" border="0" width="95%">
                <tr>
                  <td class="sectionTitle">WEB-INF Classes</td>
                </tr>        

<%
   // Note that from a servlet this would be:
   // getServletContext.getRealPath("/WEB-INF");
   //
   String path = application.getRealPath("/WEB-INF");
  
   
   if (path == null)
      out.println("<tr><td class=\"listContentNF\">Not found</td>");
   else {
      File webInf = new File(path);
      File classes = new File(webInf, "classes");
      out.println(makeHREF(classes.getPath()));
      File lib = new File(webInf, "lib");
      out.println(getJars(lib.getPath()));
   }
%>
              </table>
            </td>
          </tr>



        </table>

      </td>
    </tr>
</table>
<!-- /center -->

</body>
</html>

