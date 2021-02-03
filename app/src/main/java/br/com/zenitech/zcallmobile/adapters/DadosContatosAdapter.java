package br.com.zenitech.zcallmobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.zenitech.zcallmobile.R;
import br.com.zenitech.zcallmobile.ZCall;
import br.com.zenitech.zcallmobile.domais.DadosContatos;


public class DadosContatosAdapter extends RecyclerView.Adapter<DadosContatosAdapter.ViewHolder> {
    //private SharedPreferences prefs;
    private Context context;
    private List<DadosContatos> elementos;

    //private EntregasRepositorio entregasRepositorio;
    //private SpotsDialog dialog;

    public DadosContatosAdapter(Context context, List<DadosContatos> elementos) {
        this.context = context;
        this.elementos = elementos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        /*prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        //
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();
        criarConexao();*/
        //
        View empregoView = inflater.inflate(R.layout.item_contatos, parent, false);

        //
        return new ViewHolder(empregoView);
    }

    @Override
    public void onBindViewHolder(final DadosContatosAdapter.ViewHolder holder, int position) {

        //
        final DadosContatos dadosContatos = elementos.get(position);

        //TITULO
        TextView cliente = holder.tvCliente;
        cliente.setText(dadosContatos.nome);

        //CIDADE
        TextView endereco = holder.tvEndereco;
        endereco.setText(dadosContatos.telefone);

        holder.imageFavorito.setVisibility(View.GONE);
        if (dadosContatos.favorito.equalsIgnoreCase("1")) {
            holder.imageFavorito.setVisibility(View.VISIBLE);
        }

        holder.call.setOnClickListener(v -> {
            Intent in = new Intent(context, ZCall.class);
            //in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("nome", dadosContatos.nome);
            in.putExtra("telefone", dadosContatos.telefone);
            context.startActivity(in);
        });
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        //
        CardView rlList;
        LinearLayout llCorpo;
        TextView tvCliente;
        TextView tvEndereco;
        ImageView call, imageFavorito;

        ViewHolder(View itemView) {
            super(itemView);

            rlList = itemView.findViewById(R.id.rl_list);
            llCorpo = itemView.findViewById(R.id.llCorpo);
            tvCliente = itemView.findViewById(R.id.list_cliente);
            tvEndereco = itemView.findViewById(R.id.list_endereco);
            call = itemView.findViewById(R.id.call);
            imageFavorito = itemView.findViewById(R.id.imageFavorito);
        }
    }

    //
    /*private void criarConexao() {
        try {
            //
            DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
            //
            SQLiteDatabase conexao = dataBaseOpenHelper.getWritableDatabase();
            //
            entregasRepositorio = new EntregasRepositorio(conexao);
        } catch (SQLException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }*/
}
