################################################################################
# Configuration file for Lutece (to update while deploying)


################################################################################
# AutoInit for first deployment
# If this is true, starting the webapp set it to false and rewrite this file
# to fill in some defaults from WEB-INF/templates/admin/system/config_properties.ftl
autoInit=${autoinit}

################################################################################
# Default values for first deployment
init.webapp.prod.url=http://127.0.0.1/lutece

################################################################################
# Portal Lutece identifier

lutece.name=Lutece

# The front prod url (ex : http//mysite.org/lutece)
lutece.prod.url=

# The admin prod url (ex : http://mysite-admin.org/lutece) 
lutece.admin.prod.url=

# To force The Base URL use the following entry
#lutece.base.url=${lutece_prod_url}

################################################################################
# Portal Lutece HTTPS
lutece.https.support=0
lutece.https.url=https://localhost/lutece

################################################################################
# Multi instance handling sharing the same database 
# Don't change the default value if running a single instance
lutece.webapp.instance=default


################################################################################
# Statistical optional inclusion
lutece.statistical.include.enable=true
lutece.statistical.include.template.head=/skin/site/statistical_include_head.ftl
lutece.statistical.include.template=/skin/site/statistical_include.ftl
lutece.statistical.site.id=0
# PhpMyVisit Server
lutece.statistical.server.url=


################################################################################
# Portal and Plugins parameters
plugin.param.source.id=id_source
plugin.param.language=fr.fr

################################################################################
# Mail sending parameters (ip address of the mail server)
# username and password if authentication needed - empty otherwise)
mail.server=
mail.server.port=25
mail.username=
mail.password=
mail.list.separator=;
mail.type.plain=text/plain;charset=
mail.type.html=text/html;charset=
mail.type.calendar=text/calendar;charset=
mail.type.calendar.separator=;
mail.type.calendar.create=method=CREATE
mail.type.calendar.cancel=method=CANCEL

#default value if the no-reply email not found in datastore
mail.noreply.email=noreply@nowhere.org

mail.charset=utf-8

# mail daemon flow control : wait 'waitime' between each group of 'count' mail.
mail.daemon.waittime=1
mail.daemon.count=1000
# mail daemon : how long to wait for in case of error before retrying (see java.util.concurrent.TimeUnit)
mail.daemon.retryonerror.waittime=60
mail.daemon.retryonerror.waittime.unit=SECONDS

################################################################################
# Indexation (total->true or incremental->false)
indexer.param.total=false

################################################################################
# Properties files
file.lutece.properties=${webapp_home}/WEB-INF/conf/lutece.properties
file.webmaster.properties=${webapp_home}/WEB-INF/conf/webmaster.properties
file.jtidy.properties=${webapp_home}/WEB-INF/conf/jtidy.properties
file.dir.plugins=${webapp_home}/WEB-INF/conf/plugins

################################################################################
# Error page management
# Values of error.page.debug :
#             true      Displays the exception
#             false     Displays the message : error.page.message which is in "util_messages.properties"
#
error.page.debug=false


# In production set to number of CPU/core + 1 . ( 0 = disabled ) 
service.xmlTransformer.transformerPoolSize=2

# Time in seconds that must elapse before checking whether there is a newer version of a template file
# Default 5, in production 86400 (1 day)
service.freemarker.templateUpdateDelay=5

################################################################################
# Debug
# In production mode all values should be set to false
lutece.debug.tracePortletXml=false

################################################################################
# Site validation
file.validation.flag=${webapp_home}/WEB-INF/exploit/validation.flag
file.amin.validation.flag=${webapp_home}/WEB-INF/exploit/admin_validation.flag


################################################################################
# log4j parameters
# The following loggers should be set to ERROR level in production mode

########                         L O G G E R S                         #########                            
# Syntax log4j.logger.<logger name>=<level>, <appender 1>, <appender 2>, ...

log4j.rootLogger=ERROR, Error
log4j.logger.lutece=DEBUG, Console
log4j.logger.lutece.event=INFO, Application, Console
log4j.additivity.lutece.event=false
log4j.logger.lutece.error=ERROR, Error, Console
log4j.additivity.lutece.error=false
log4j.logger.lutece.debug=ERROR, Console
log4j.additivity.lutece.debug=false
log4j.logger.lutece.debug.xmlContent=ERROR, Console
log4j.logger.lutece.debug.sql=ERROR, Console
log4j.logger.lutece.pool=ERROR, Pool
log4j.logger.lutece.mail=INFO, Mail
log4j.logger.lutece.security=WARN, Security
log4j.logger.lutece.adminaccess=INFO, AdminAccess
log4j.logger.servlet=ERROR, Error
log4j.logger.session=ERROR, Error
log4j.logger.compiler=ERROR, Error
log4j.logger.digester=ERROR, Error
log4j.logger.beanutils=ERROR, Error
log4j.logger.freemarker=ERROR, Error
log4j.logger.net.sf.ehcache=ERROR, Error
log4j.logger.httpclient=ERROR, Error
log4j.logger.org.apache.pdfbox=ERROR, PDF
log4j.logger.lutece.jpa=INFO, JPA
log4j.logger.org.hibernate=INFO, JPA
log4j.logger.org.springframework.orm.jpa=INFO, JPA
log4j.logger.org.springframework.transaction=INFO, JPA
log4j.logger.lutece.indexer=ERROR, Indexer
log4j.logger.org.apache.tika=ERROR, Indexer

########                       A P P E N D E R S                       #########                            

# File "error.log"
log4j.appender.Error=org.apache.log4j.RollingFileAppender
log4j.appender.Error.File=${webapp_home}/WEB-INF/logs/error.log
log4j.appender.Error.Append=true
log4j.appender.Error.layout=org.apache.log4j.PatternLayout
log4j.appender.Error.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.Error.MaxFileSize=1000KB
log4j.appender.Error.MaxBackupIndex=5

# File "application.log"
log4j.appender.Application=org.apache.log4j.RollingFileAppender
log4j.appender.Application.File=${webapp_home}/WEB-INF/logs/application.log
log4j.appender.Application.Append=true
log4j.appender.Application.layout=org.apache.log4j.PatternLayout
log4j.appender.Application.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.Application.MaxFileSize=1000KB
log4j.appender.Application.MaxBackupIndex=5

# File "pool.log"
log4j.appender.Pool=org.apache.log4j.RollingFileAppender
log4j.appender.Pool.File=${webapp_home}/WEB-INF/logs/pool.log
log4j.appender.Pool.Append=true
log4j.appender.Pool.layout=org.apache.log4j.PatternLayout
log4j.appender.Pool.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.Pool.MaxFileSize=1000KB
log4j.appender.Pool.MaxBackupIndex=5

# File "mail.log"
log4j.appender.Mail=org.apache.log4j.RollingFileAppender
log4j.appender.Mail.File=${webapp_home}/WEB-INF/logs/mail.log
log4j.appender.Mail.Append=true
log4j.appender.Mail.layout=org.apache.log4j.PatternLayout
log4j.appender.Mail.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.Mail.MaxFileSize=1000KB
log4j.appender.Mail.MaxBackupIndex=5

# File "PDF.log"
log4j.appender.PDF=org.apache.log4j.RollingFileAppender
log4j.appender.PDF.File=${webapp_home}/WEB-INF/logs/PDF.log
log4j.appender.PDF.Append=true
log4j.appender.PDF.layout=org.apache.log4j.PatternLayout
log4j.appender.PDF.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.PDF.MaxFileSize=1000KB
log4j.appender.PDF.MaxBackupIndex=5

# File "security.log"
log4j.appender.Security=org.apache.log4j.RollingFileAppender
log4j.appender.Security.File=${webapp_home}/WEB-INF/logs/security.log
log4j.appender.Security.Append=true
log4j.appender.Security.layout=org.apache.log4j.PatternLayout
log4j.appender.Security.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.Security.MaxFileSize=1000KB
log4j.appender.Security.MaxBackupIndex=5

# File "jpa.log"
log4j.appender.JPA=org.apache.log4j.RollingFileAppender
log4j.appender.JPA.File=${webapp_home}/WEB-INF/logs/jpa.log
log4j.appender.JPA.Append=true
log4j.appender.JPA.layout=org.apache.log4j.PatternLayout
log4j.appender.JPA.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.JPA.MaxFileSize=1000KB
log4j.appender.JPA.MaxBackupIndex=5

# File "indexer.log"
log4j.appender.Indexer=org.apache.log4j.RollingFileAppender
log4j.appender.Indexer.File=${webapp_home}/WEB-INF/logs/indexer.log
log4j.appender.Indexer.Append=true
log4j.appender.Indexer.layout=org.apache.log4j.PatternLayout
log4j.appender.Indexer.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.Indexer.MaxFileSize=1000KB
log4j.appender.Indexer.MaxBackupIndex=5

# Console
log4j.appender.Console=org.apache.log4j.ConsoleAppender
log4j.appender.Console.layout=org.apache.log4j.PatternLayout
log4j.appender.Console.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n

# Email
log4j.appender.SendMail=org.apache.log4j.net.SMTPAppender
log4j.appender.SendMail.SMTPHost=
#log4j.appender.SendMail.SMTPUsername=
#log4j.appender.SendMail.SMTPPassword=
log4j.appender.SendMail.Subject=${sendmail_subject!}
log4j.appender.SendMail.Threshold=ERROR
log4j.appender.SendMail.BufferSize=10
log4j.appender.SendMail.From=noreply@nowhere.org
log4j.appender.SendMail.To=admin@nowhere.org
log4j.appender.SendMail.layout=org.apache.log4j.PatternLayout
log4j.appender.SendMail.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %-5p [%t] %c{2} - %m%n

# Admin Access
# File "admin_access.log"
log4j.appender.AdminAccess=org.apache.log4j.RollingFileAppender
log4j.appender.AdminAccess.File=${webapp_home}/WEB-INF/logs/admin_access.log
log4j.appender.AdminAccess.Append=true
log4j.appender.AdminAccess.layout=org.apache.log4j.PatternLayout
log4j.appender.AdminAccess.layout.ConversionPattern=%d{dd/MM/yy HH:mm:ss} %m%n
log4j.appender.AdminAccess.MaxFileSize=1000KB
log4j.appender.AdminAccess.MaxBackupIndex=5

