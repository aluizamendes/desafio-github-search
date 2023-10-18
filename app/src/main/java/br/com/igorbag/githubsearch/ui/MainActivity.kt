package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var tvEmptyStateRepos: TextView
    lateinit var tvEmptyStateUser: TextView
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupRetrofit()
        setupListeners()

        val cachedUsername = getChachedUserName()
        if (cachedUsername != null) {
            nomeUsuario.setText(cachedUsername)
//            getAllReposByUserName(cachedUsername)
        }
    }

    // Encontra os elementos da activity pelo ID
    fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        tvEmptyStateRepos = findViewById(R.id.tv_user_empty_repos)
        tvEmptyStateUser = findViewById(R.id.tv_empty_state_user)
    }


    // Metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        // Colocar a ação de click do botao confirmar

        btnConfirmar.setOnClickListener {
            val userString = nomeUsuario.text.toString()
            saveUserLocal(userString)
            getAllReposByUserName(userString)
        }
    }

    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(user: String) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.saved_user), user)
            apply()
        }
    }

    private fun getChachedUserName(): String? {
        // Depois de persistir o usuario exibir sempre as informacoes no EditText se a sharedpref possuir algum valor
        val userSharedPref = getPreferences(Context.MODE_PRIVATE)
        var userFromSharedPref = userSharedPref.getString(getString(R.string.saved_user), "")

        if (userSharedPref == null || userSharedPref.equals("")) {
            userFromSharedPref = null

        }
        return userFromSharedPref
    }

    // Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        val url = "https://api.github.com/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    // Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName(user: String) {
        // Realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        githubApi.getAllRepositoriesByUser(user).enqueue(object: Callback<List<Repository>> {

            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                if (response.isSuccessful) {
                    // chamada feita com sucesso, mostra os repositorios
                    tvEmptyStateRepos.visibility = View.GONE
                    tvEmptyStateUser.visibility = View.GONE
                    listaRepositories.visibility = View.VISIBLE

                    response.body()?.let { setupAdapter(it) }

                    // se o usuário é encontrado mas repositórios estão vazios
                    if (response.body()?.isEmpty() == true) {
                        tvEmptyStateUser.visibility = View.GONE
                        tvEmptyStateRepos.visibility = View.VISIBLE
                        Log.i("Resposta API ->", "User não tem repos")
                    }

                    // usuário não encontrado
                } else if (response.code() == 404) {
                    tvEmptyStateRepos.visibility = View.GONE
                    listaRepositories.visibility = View.GONE
                    tvEmptyStateUser.visibility = View.VISIBLE
                    Log.d("Resposta 404 ->", "Usuário não encontrado")

                } else if (response.code() == 403) {
                    Log.e("Resposta API 403 ->", "Limite de requisicoes excedido")
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                Log.e("API Call", "Falha na chamada à API", t)
            }

        })
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        listaRepositories.visibility = View.VISIBLE

        // criar o adapter
        val repoAdapter = RepositoryAdapter(list)
        listaRepositories.adapter = repoAdapter
    }

    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}