<#-- Shows the translations applied to a doc through the translatable aspect -->
<b>Translatable:</b>
<#if document?exists>
   <#if hasAspect(document, "cm:translatable") = 1>
      Yes<br>
      <table>
      <#if document.assocs["cm:translations"]?exists>
         <#list document.assocs["cm:translations"] as t>
            <tr><td>${t.content}</td></tr>
         </#list>
      </#if>
      </table>
   <#else>
      No<br>
   </#if>
<#else>
   No document found!
</#if>
