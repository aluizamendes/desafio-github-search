package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var openBrowser: (urlRepository: String) -> Unit = {}
    var shareRepositoryLink: (urlRepository: String) -> Unit = {}

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val urlRepository = repositories[position].htmlUrl

        holder.nomeRepositorio.text = repositories[position].name

        holder.btnCompartilhar.setOnClickListener {
            shareRepositoryLink(urlRepository)
        }

        holder.itemCard.setOnClickListener {
            openBrowser(urlRepository)
        }
    }

    override fun getItemCount(): Int = repositories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // Implementar o ViewHolder para os repositorios
        val nomeRepositorio: TextView = view.findViewById(R.id.tv_nome_repositorio)
        val btnCompartilhar: ImageView = view.findViewById(R.id.iv_compartilhar)
        val itemCard: CardView = view.findViewById(R.id.cv_car)
    }
}


