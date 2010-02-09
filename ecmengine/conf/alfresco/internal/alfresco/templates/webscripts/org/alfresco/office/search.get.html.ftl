<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.n?exists><#assign node=args.n><#else><#assign node=companyhome></#if>
<#if args.e?exists><#assign extn=args.e><#else><#assign extn="doc"></#if><#assign extnx=extn+"x">
<#if args.n?exists><#assign nav=args.n><#else><#assign nav=""></#if>
<#if (args.searchagain?exists)><#assign searchText=args.searchagain><#else><#assign searchText=""></#if>
<#if (args.maxresults?exists)><#assign maxResults=args.maxresults><#else><#assign maxResults="5"></#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<#assign searchCommand="OfficeSearch.runSearch('${url.serviceContext}/office/searchResults', '${defaultQuery}')" >
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>Browse Spaces and Documents</title>
	<link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
	<script type="text/javascript" src="${url.context}/scripts/office/search.js"></script>
   <script type="text/javascript">//<![CDATA[
      OfficeAddin.defaultQuery = '${defaultQuery}';
   //]]></script>
</head>
<body>
<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco${defaultQuery?html}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="My Alfresco" /></span></a></li>
      <li><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}"><span><img src="${url.context}/images/office/navigator.gif" alt="Browse Spaces and Documents" /></span></a></li>
      <li id="current"><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search${defaultQuery?html}"><span><img src="${url.context}/images/office/search.gif" alt="Search Alfresco" /></span></a></li>
      <li><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails${defaultQuery?html}"><span><img src="${url.context}/images/office/document_details.gif" alt="View Details" /></span></a></li>
      <li><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks${defaultQuery?html}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="My Tasks" /></span></a></li>
      <li><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags${defaultQuery?html}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
</div>

<div class="header">Search</div>

<div class="containerSearchTerms">
   <div id="nonStatusText">
      <div class="searchBox">
         <span class="searchParam">
            Search for
            <input type="text" id="searchText" value="${searchText}" maxlength="512" />
         </span>
         <span>
            <a id="simpleSearchButton" class="taskAction" href="#" onclick="${searchCommand}">Search</a>
         </span>
         <span class="searchParam">
            Return a maximum of
            <select id="maxResults" name="maxResults" onchange="${searchCommand}">
               <option <#if maxResults="5">selected="selected" </#if>value="5">5</option>
               <option <#if maxResults="10">selected="selected" </#if>value="10">10</option>
               <option <#if maxResults="15">selected="selected" </#if>value="15">15</option>
               <option <#if maxResults="20">selected="selected" </#if>value="20">20</option>
               <option <#if maxResults="50">selected="selected" </#if>value="50">50</option>
            </select>&nbsp;items
         </span>
      </div>
   </div>
   
   <div id="statusText"></div>
   
</div>

<div class="header"><span id="itemsFound">&nbsp;</span></div>

<div id="resultsList" class="containerSearchResults">
   <div id="searchResultsList"></div>
</div>

<#if (args.searchagain?exists)>
<script type="text/javascript">
   window.addEvent('domready', function(){${searchCommand}});
</script>
</#if>
</body>
</html>
