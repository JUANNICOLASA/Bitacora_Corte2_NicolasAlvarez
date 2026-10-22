import Ornamento from '../../components/ui/Ornamento'
import ImagenConRespaldo from '../../components/ui/ImagenConRespaldo'

const principios = [
  {
    titulo: 'Cada destilado con nombre propio',
    texto: 'Ningún cóctel con alcohol sale de la barra sin que elijas la marca o el tipo exacto de destilado.'
  },
  {
    titulo: 'Sin alcohol, sin concesiones',
    texto: 'Nuestros mocktails tienen su propia carta y bloqueamos cualquier adición con alcohol para cuidarte.'
  },
  {
    titulo: 'Información que no depende del color',
    texto: 'Si algo se agota, lo verás escrito y marcado con un candado, no solo pintado de rojo.'
  }
]

export default function ElBar() {
  return (
    <section id="el-bar" className="seccion el-bar" aria-labelledby="el-bar-titulo">
      <div className="el-bar__texto">
        <Ornamento />
        <h2 id="el-bar-titulo" className="titulo-seccion">Un lugar para quedarse un poco más</h2>
        <p className="parrafo-destacado">
          Blue Velvet es una coctelería de alto nivel pensada para la noche: luz baja, madera oscura, jazz en vivo
          y una barra que trabaja con el inventario real en cada pedido.
        </p>
        <p>
          Preparamos cada trago frente a ti, con destilados que puedes elegir y cambiar una vez antes de que el
          bartender empiece. La cocina acompaña con platos para compartir sin prisa.
        </p>
        <dl className="principios">
          {principios.map((principio) => (
            <div key={principio.titulo} className="principios__item">
              <dt>{principio.titulo}</dt>
              <dd>{principio.texto}</dd>
            </div>
          ))}
        </dl>
      </div>
      <div className="el-bar__imagen">
        <ImagenConRespaldo src="/img/ambiente/bartender.jpg" alt="Bartender preparando un cóctel en la barra" />
      </div>
    </section>
  )
}
