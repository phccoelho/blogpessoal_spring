package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;
import com.generation.blogpessoal.util.TestBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	private static final String USUARIO_ROOT_EMAIL = "root@email.com";
	private static final String USUARIO_ROOT_SENHA = "rootroot";
	private static final String BASE_URL_USUARIOS = "/usuarios";
	
	@BeforeAll
	void start() {
		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuarioRoot());
	}
	
	@Test
	@DisplayName("Deve Cadastrar um Novo Usuário com sucesso")
	public void deveCadastrarUsuario() {
		
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Pedro Coelho", "pedrocoelho@email.com.br", "12345678");
		
		//When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL_USUARIOS + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		//Then
		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertEquals("Pedro Coelho", resposta.getBody().getNome());
		assertEquals("pedrocoelho@email.com.br", resposta.getBody().getUsuario());
	}
	
	@Test
	@DisplayName("Não Deve permitir a duplicação do Usuário")
	public void naoDeveDuplicarUsuario() {
		
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Iago Zandone", "iagozandone@email.com.br", "12345678");
		usuarioService.cadastrarUsuario(usuario);
		
		//When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL_USUARIOS + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		//Then
		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
	}
	@Test
	@DisplayName("Deve atualizar os dados de um usuário com sucesso")
	public void deveAtualizarUmUsuario() {
		
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Gabriel Souza", "gabrielsouza@email.com.br", "12345678");
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(usuario);
		
		var usuarioUpdate = TestBuilder.criarUsuario(usuarioCadastrado.get().getId(),"Gabriel Souza Eduardo", "gabrielsouza_ed@email.com.br", "12345678");
		
		//When
		HttpEntity<Usuario> requisicao = new HttpEntity<Usuario>(usuarioUpdate);
		ResponseEntity<Usuario> resposta = testRestTemplate
				.withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
				.exchange(BASE_URL_USUARIOS + "/atualizar", HttpMethod.PUT, requisicao, Usuario.class);
		
		//Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertEquals("Gabriel Souza Eduardo", resposta.getBody().getNome());
		assertEquals("gabrielsouza_ed@email.com.br", resposta.getBody().getUsuario());
	}
	
	@Test
	@DisplayName(" Deve listar todos os usuários com sucesso")
	public void deveListarUmUsuario() {
		//Given
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Jovani Almeida","jovani_almeida@email.com", "senha123"));
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Carlos Garcia","carlos_garcia@email.com", "senha123"));

		//When
		ResponseEntity<Usuario[]> resposta = testRestTemplate
				.withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
				.exchange(BASE_URL_USUARIOS + "/all", HttpMethod.GET, null, Usuario[].class);
		//Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}
 
}

