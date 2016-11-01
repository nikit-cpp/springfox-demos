package springfoxdemo.staticdocs

import groovy.io.FileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import springfox.documentation.staticdocs.Swagger2MarkupResultHandler

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ContextConfiguration(
        loader = SpringApplicationContextLoader,
        classes = Application)
@WebAppConfiguration
@TestExecutionListeners([DependencyInjectionTestExecutionListener, DirtiesContextTestExecutionListener])
class StaticDocsTest extends spock.lang.Specification {

  @Autowired
  WebApplicationContext context;

  MockMvc mockMvc;

  def setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build()
  }

  // I just add saving json to build/swagger_json
  def "generates the petstore api asciidoc"() {
    setup:
      String outDir = System.getProperty('asciiDocOutputDir', 'build/aciidoc')
      Swagger2MarkupResultHandler resultHandler = Swagger2MarkupResultHandler
              .outputDirectory(outDir)
              .build()

    when:
      String out = this.mockMvc.perform(get("/v2/api-docs").accept(MediaType.APPLICATION_JSON))
              .andDo(resultHandler)
              .andExpect(status().isOk()).andReturn().response.contentAsString
      String SWAGGER_OUTPUT_DIR = "build/swagger_json"
      String SWAGGER_JSON_FILE = "swagger.json"
      Files.createDirectories(Paths.get(SWAGGER_OUTPUT_DIR));
      BufferedWriter writer = Files.newBufferedWriter(Paths.get(SWAGGER_OUTPUT_DIR, SWAGGER_JSON_FILE), StandardCharsets.UTF_8)
      try {
        writer.write(out); // for debug purposes
      } finally {
        writer.close()
      }

    then:
      def list = []
      def dir = new File(resultHandler.outputDir)
      dir.eachFileRecurse(FileType.FILES) { file ->
        list << file.name
      }
      list.sort() == ['definitions.adoc', 'overview.adoc', 'paths.adoc']
  }

  def "check for field 'type' exists for @RequestParam Map<String, String>"() {
    def mockResult
    when: {
      mockResult = this.mockMvc.perform(get("/v2/api-docs").accept(MediaType.APPLICATION_JSON)).
          andExpect(status().isOk())
    }
    then: {
      mockResult.
              andExpect(jsonPath('$.paths./fail.post.parameters[0].name', is('requestParameters'))). // I just check what $.paths./fail works
              andExpect(jsonPath('$.paths./fail.post.parameters[0].type', notNullValue()))
    }
  }
}
