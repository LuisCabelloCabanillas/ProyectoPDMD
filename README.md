# ProyectoPMDM
En este proyecto vamos a hacer un trabajo basado en una aplicación de recetas, en la cual podremos ver varios tipos de recetas que una vez que pulsemos en una carta(Button) nos redigirá hacia una pestaña que estará conectada con una base de datos que tendrá el nombre, los productos necesarios, como hacer la receta. En la misma activity existirá un botón que nos pedirá permisos para dirigirse a Google y poder ver más información sobre la receta.

La base de datos esta creada en FireBase ya que es más sencilla para el trabajo os dejo como usar ponerlo en el android studio 
Aquí dejo lo que me paso ChatGPT para conectarlo con una nueva firebase, de esto solo debes de hacer la conexión con el android studio(Punto 2)(Termina en la línea 292).
Perfecto. Te voy a dar **un paso a paso claro y completo** para que podáis integrar Firebase en vuestra app de Android Studio **desde cero hasta tener login, registro y CRUD de recetas funcionando**.

Voy a dividirlo en 6 partes:

1. **Crear el proyecto en Firebase**
2. **Conectar Firebase con Android Studio**
3. **Configurar Firebase Authentication**
4. **Configurar Firestore para las recetas**
5. **Hacer las Activities de Login y Registro**
6. **Hacer las Activities de recetas (Lista, Añadir, Detalle)**

Vamos paso a paso 👇

---

# 🔥 **1. Crear el proyecto en Firebase**

1. Entra en: [https://console.firebase.google.com](https://console.firebase.google.com)
2. Clic en **"Crear un proyecto"**
3. Ponle un nombre → ejemplo: `RecetasApp`
4. Desactiva Google Analytics si quieres
5. Crea el proyecto (tarda 5–10s)

---

# 🔥 **2. Conectar Firebase con Android Studio**

1. Abre Android Studio
2. Ve a:
   **Tools → Firebase**
3. Se abre una ventana a la derecha
4. Selecciona **Authentication** → **Email/Password Authentication**
5. Clic en **"Connect your app to Firebase"**
6. Selecciona el proyecto creado
7. Clic en **"Add Firebase Authentication to your app"**

Esto añadirá automáticamente:

✔ `google-services.json`
✔ dependencias de Firebase
✔ configuración en tu `build.gradle`

---

# 🔥 **3. Configurar Firebase Authentication**

En Firebase Console:

1. Ve a **Authentication**
2. Pestaña **Método de inicio de sesión**
3. Habilita **Correo/Contraseña**

✔ Listo para registrar y hacer login.

---

# 🔥 **4. Configurar Firestore para guardar recetas**

1. En Firebase Console → **Firestore Database**
2. Clic en **Crear Base de datos**
3. Modo: **Modo de prueba** (seguro para un proyecto escolar)
4. Región: la que te recomiende
5. Crear

La colección la crearéis desde Android automáticamente.

---

## 🔐 Reglas recomendadas (seguras y fáciles)

Ve a **Reglas** y pon esto:

```
// Solo usuarios autenticados pueden leer y escribir recetas
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /recetas/{document} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

# 🔥 **5. Activities de Login y Registro (Android)**

## ➤ Dependencias necesarias

En `app/build.gradle`:

```gradle
implementation 'com.google.firebase:firebase-auth:22.1.1'
implementation 'com.google.firebase:firebase-firestore:25.0.0'
```

---

# 🟦 **Activity: Registro**

### XML (simplificado)

```xml
<EditText android:id="@+id/email" ... />
<EditText android:id="@+id/password" ... />
<EditText android:id="@+id/nombre" ... />
<Button android:id="@+id/btnRegistrar" ... />
```

### Java/Kotlin

```java
FirebaseAuth auth = FirebaseAuth.getInstance();

btnRegistrar.setOnClickListener(v -> {
    String email = emailTxt.getText().toString();
    String pass = passTxt.getText().toString();

    auth.createUserWithEmailAndPassword(email, pass)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
});
```

---

# 🟩 **Activity: Login**

### XML:

```xml
<EditText android:id="@+id/email" ... />
<EditText android:id="@+id/password" ... />
<Button android:id="@+id/btnLogin" ... />
```

### Java/Kotlin

```java
FirebaseAuth auth = FirebaseAuth.getInstance();

btnLogin.setOnClickListener(v -> {
    String email = emailTxt.getText().toString();
    String pass = passTxt.getText().toString();

    auth.signInWithEmailAndPassword(email, pass)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(this, ListaRecetasActivity.class));
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
});
```

---

# 🔥 **6. CRUD de recetas con Firestore**

---

# 🟧 Modelo Receta

```java
public class Receta {
    public String titulo;
    public String descripcion;
    public String url;
    public String autor;

    public Receta() {} // Obligatorio Firestore

    public Receta(String titulo, String descripcion, String url, String autor) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.url = url;
        this.autor = autor;
    }
}
```

---

# 🟦 Activity: Lista de Recetas

Usa un **RecyclerView**.

### Obtener recetas:

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();

db.collection("recetas")
    .get()
    .addOnSuccessListener(query -> {
        List<Receta> lista = new ArrayList<>();
        for (DocumentSnapshot doc : query) {
            Receta r = doc.toObject(Receta.class);
            lista.add(r);
        }
        adapter.setData(lista);
    });
```

---

# 🟩 Activity: Añadir Receta

### XML con:

* EditText → título
* EditText → descripción
* EditText → url
* Botón → guardar

### Java/Kotlin

```java
FirebaseFirestore db = FirebaseFirestore.getInstance();
FirebaseAuth auth = FirebaseAuth.getInstance();

btnGuardar.setOnClickListener(v -> {
    Receta r = new Receta(
        titulo.getText().toString(),
        descripcion.getText().toString(),
        url.getText().toString(),
        auth.getCurrentUser().getUid()
    );

    db.collection("recetas")
        .add(r)
        .addOnSuccessListener(ref -> {
            Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show();
            finish();
        });
});
```

---

# 🟧 Activity: Detalle de Receta

### Recibes la receta al pulsar en la lista

Y pones un botón:

```java
btnAbrirGoogle.setOnClickListener(v -> {
    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(receta.url));
    startActivity(i);
});
```

---

# 🎉 ¡Listo!

Con esto tu app ya tendrá:

✔ Registro
✔ Login
✔ Pantalla de recetas
✔ Detalle de receta
✔ Abrir enlace en Google
✔ Añadir recetas globalmente
✔ Todo gratis
✔ Todo sin backend

---

# ⭐ Si quieres, ahora puedo darte:

* El diseño XML para cada pantalla
* El adaptador del RecyclerView
* Toda la estructura de carpetas
* Plantilla completa del proyecto

¿Quieres que te escriba **todo el proyecto completo** organizado por carpetas para copiar-pegar?


Desde aquí es un resumen de como usar todo para que quede más claro
Guía para trabajar en grupo en Android Studio con Firebase y Kotlin
1️⃣ Crear un proyecto de Firebase y Android Studio

Uno de vosotros crea el proyecto en Firebase Console
:

Click en “Add project”.

Ponle un nombre, acepta términos, no habilites Google Analytics si es solo para práctica.

Una vez creado, añade la app de Android:

Package name = el que usaréis en Android Studio (com.tuapp.recetas).

Descarga el google-services.json y ponlo en app/ de vuestro proyecto.

En Android Studio:

En build.gradle (app), añade:

implementation platform('com.google.firebase:firebase-bom:33.3.0')
implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'


Sincroniza el proyecto.

2️⃣ Configurar Firebase Authentication

En Firebase Console → Authentication → Get started.

Activar Email/Password.

Esto permitirá que todos podáis registrar y loguear usuarios directamente desde la app.

3️⃣ Configurar Firestore

En Firebase Console → Firestore Database → Create database.

Modo test, para que todos podáis escribir sin reglas complicadas (solo para práctica).

Creamos la colección recetas.

Cada documento será una receta con campos:

titulo (String)

descripcion (String)

url (String)

autor (String → UID del usuario que la creó)

4️⃣ Configurar Git para trabajo en equipo

Uno de vosotros crea un repositorio en GitHub (privado si queréis).

Cada miembro:

git clone <URL_DEL_REPO>


En Android Studio:

VCS → Enable Version Control Integration → Git

Buenas prácticas:

Cada miembro crea su branch para cambios:

git checkout -b nombre_miembro


Hacer commits frecuentes con mensajes claros:

git add .
git commit -m "Añadida pantalla de login en Kotlin"


Subir cambios al remoto:

git push origin nombre_miembro


Antes de mezclar cambios, hacer pull y resolver conflictos si los hay.

Una vez listo, mergear al main:

Crear Pull Requests desde GitHub y revisarlo entre vosotros.

5️⃣ Evitar conflictos con Firebase

Solo un google-services.json en el proyecto.

Todos los miembros usan el mismo Firebase project.

Evitad cambiar las reglas de Firestore si no es necesario.

Cada miembro prueba en su emulador, los datos se sincronizan automáticamente en la nube.

6️⃣ Flujo de trabajo recomendado

Usuario abre la app:

Si pulsa Registro → RegistroActivity.

Si pulsa Login → LoginActivity.

Una vez logueado:

Va a ListaRecetasActivity → muestra todas las recetas.

Puede pulsar una receta → DetalleRecetaActivity.

Puede pulsar Agregar Receta → AgregarRecetaActivity.

Todos los cambios se reflejan automáticamente en Firestore → todos los miembros ven las nuevas recetas al abrir la app.

7️⃣ Consejos para trabajar en grupo

Cada miembro se encarga de una pantalla o funcionalidad:

Miembro 1 → Registro/Login.

Miembro 2 → Lista y Detalle de recetas.

Miembro 3 → Agregar receta.

Revisad pull requests antes de mergear.

Usad nombres de variables consistentes y Kotlin idiomático (val, var, data class).

Haced pruebas con varios usuarios para verificar autenticación y Firestore.