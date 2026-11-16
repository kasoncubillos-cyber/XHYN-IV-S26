package com.libertyrecomp;

import android.app.*;
import android.widget.*;
import java.util.function.IntPredicate;

/** Requests retail cheat natives on the game thread after resuming. */
final class CheatsUi {
    private static final int[] IDS={2,0,1,4,3,6,7,8,9,10,11,12,13,14,15,100,101,102,103,104,105,106,107,108,109};
    private static final String[] NAMES={"Salud, chaleco y municiones","Armas · paquete 1","Armas · paquete 2",
        "Quitar estrellas de policía","Aumentar búsqueda policial","Cambiar clima","Generar helicóptero Annihilator","Generar moto NRG 900",
        "Generar FIB Buffalo","Generar lancha Jetmax","Generar Comet","Generar Turismo","Generar Cognoscenti","Generar Super GT","Generar moto Sanchez","Restaurar salud","Añadir chaleco antibalas","Reparar vehículo actual","Añadir $10.000","Activar inmortalidad","Desactivar inmortalidad","Cambiar a la mañana","Cambiar al mediodía","Cambiar al atardecer","Cambiar a medianoche"};
    static AlertDialog show(Activity a,MobileSettingsUi.Runtime runtime,IntPredicate request,Runnable resume){
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);
        body.addView(ModernUi.text(a,"Solo GTA IV, modo individual. Activa un truco jugando en modo libre. La orden se ejecuta al volver al juego, si el motor la admite; ponerla en cola no confirma su efecto. Algunos trucos afectan los logros o el progreso. Guarda tu partida antes de usarlos.",13,ModernUi.MUTED));
        body.addView(ModernUi.text(a,"Última solicitud: "+runtime.get("gta4_cheat_status"),12,ModernUi.MUTED));
        CheckBox override=new CheckBox(a);override.setText("Permitir logros al usar trucos");override.setTextColor(ModernUi.TEXT);
        override.setChecked("true".equals(runtime.get("gta4_cheat_achievements")));body.addView(override);
        final boolean[] updating={false};
        override.setOnCheckedChangeListener((button,on)->{
            if(updating[0])return;
            if(!runtime.apply(new String[]{"gta4_cheat_achievements"},new String[]{Boolean.toString(on)})){
                updating[0]=true;button.setChecked(!on);updating[0]=false;
                Toast.makeText(a,"No se pudo guardar la preferencia de logros.",Toast.LENGTH_LONG).show();
            }
        });
        body.addView(ModernUi.text(a,"Desactivado respeta las restricciones originales. Activado solo permite eventos de logros locales; no desbloquea logros automáticamente ni sincroniza con Xbox Live.",12,ModernUi.MUTED));
        final AlertDialog[] current={null};
        LinearLayout grid=new LinearLayout(a);grid.setOrientation(LinearLayout.VERTICAL);body.addView(grid);
        final String[] category={"Todos"};CompactFilterBar filters=new CompactFilterBar(a,"Buscar truco");final EditText search=filters.search;
        body.removeView(grid);body.addView(filters);body.addView(grid);
        final Runnable[] render={null};java.util.List<Button> buttons=new java.util.ArrayList<>();String[] categories={"Todos","Jugador","Armas","Policía","Vehículos","Mundo"};
        render[0]=()->{grid.removeAllViews();for(int n=0;n<buttons.size();n++)ModernUi.selected(buttons.get(n),categories[n].equals(category[0]));String query=search.getText().toString().toLowerCase(java.util.Locale.ROOT);for(int i=0;i<IDS.length;i++){final int index=i;int id=IDS[i];String group=id==0||id==1?"Armas":id==3||id==4?"Policía":id>=7&&id<=15||id==102?"Vehículos":id==6||id>=106?"Mundo":"Jugador";if(!category[0].equals("Todos")&&!category[0].equals(group)||!NAMES[i].toLowerCase(java.util.Locale.ROOT).contains(query))continue;ModernUi.gridAction(a,grid,NAMES[i],()->{
            if(request.test(IDS[index])){current[0].setOnDismissListener(null);current[0].dismiss();resume.run();Toast.makeText(a,"Truco solicitado: "+NAMES[index],Toast.LENGTH_SHORT).show();}
            else Toast.makeText(a,runtime.get("gta4_cheat_status"),Toast.LENGTH_LONG).show();
        });}};
        for(String name:categories)buttons.add(filters.add(a,name,()->{category[0]=name;render[0].run();}));
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){render[0].run();}public void afterTextChanged(android.text.Editable e){}});render[0].run();
        AlertDialog dialog=ModernUi.dialog(a,"MENÚ MOD · TRUCOS",body);current[0]=dialog;dialog.show();return dialog;
    }
    private CheatsUi(){}
}
