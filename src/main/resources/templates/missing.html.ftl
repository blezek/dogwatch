A watch named ${watch.name} is missing.
<#if watch.next_check??>Was expected to report in at ${watch.next_check}.<#else>No further watches are scheduled, too many missed watches.</#if>
