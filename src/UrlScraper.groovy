@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT


def testoutput = new File("my_home_timeline_and_pages_it3")


new File("my_home_timeline").eachLine { String line ->

	def slurper = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())
	StringBuilder titles = new StringBuilder()
		
	try{
	
		line.split(";")[6].split(" ").findAll{ it.startsWith("http") }.each{ url ->
		
		println "******** $url *******************************"
		
		def http = new HTTPBuilder(url)
		
		http.request(GET,TEXT) { req ->
			//uri.path = '/mail/help/tasks/' // overrides any path in the default URL
			headers.'User-Agent' = 'Mozilla/5.0'
			
			response.success = { resp, reader ->
				if(!(resp.contentType + "").contains("image")){
					def html = slurper.parse( reader )
					titles.append( (html.head.title) ).append(" ")
					      .append( html.'**'.findAll{ tag -> ["h1","h2","p"].contains( tag.name() ) }
					 		                     .collect{ it.text() }.join(" ") )
				}
				println titles
			}
			response.failure = { resp ->
				println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
			 }
		  }
		}
	} catch(e){ //embrace failure and continue
		e.printStackTrace()
	}
	
	testoutput.append( line + ";" + titles.replaceAll("\\s+"," ").replaceAll("'","") + "\n" )
}



