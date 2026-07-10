<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { RoomEnvironment } from 'three/examples/jsm/environments/RoomEnvironment.js'
import brainUrl from '@/assets/models/human-brain.glb?url'

const container = ref<HTMLElement | null>(null)
const loaded = ref(false)

let renderer: THREE.WebGLRenderer
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let controls: OrbitControls
let group: THREE.Group
const disposables: Array<{ dispose: () => void }> = []

let raf = 0
let running = false
let entrance = 0
let entranceArmed = false
const clock = new THREE.Clock()


const ptr = { x: 0, y: 0, tx: 0, ty: 0 }

let ro: ResizeObserver
let io: IntersectionObserver

const easeOutCubic = (t: number) => 1 - Math.pow(1 - t, 3)

const onPointer = (e: PointerEvent) => {
  const el = container.value!
  const r = el.getBoundingClientRect()
  ptr.tx = ((e.clientX - r.left) / r.width) * 2 - 1
  ptr.ty = ((e.clientY - r.top) / r.height) * 2 - 1
}

const loop = () => {
  if (!running) return
  raf = requestAnimationFrame(loop)

  const t = clock.getElapsedTime()

  if (entranceArmed && entrance < 1) entrance = Math.min(1, entrance + 0.018)
  const e = easeOutCubic(entrance)

  if (group) {
    group.position.y = Math.sin(t * 0.7) * 0.1
    group.scale.setScalar(0.55 + 0.45 * e)
    // extra entrance spin that settles + subtle pointer tilt
    ptr.x += (ptr.tx - ptr.x) * 0.05
    ptr.y += (ptr.ty - ptr.y) * 0.05
    group.rotation.y = (1 - e) * Math.PI * 1.2 + ptr.x * 0.35
    group.rotation.x = ptr.y * 0.2
  }

  controls.update()
  renderer.render(scene, camera)
}

const start = () => {
  if (running) return
  running = true
  clock.getDelta()
  raf = requestAnimationFrame(loop)
}
const stop = () => {
  running = false
  if (raf) cancelAnimationFrame(raf)
  raf = 0
}

onMounted(() => {
  const el = container.value!
  const w = el.clientWidth || 1
  const h = el.clientHeight || 1

  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(35, w / h, 0.1, 100)
  camera.position.set(0, 0, 6.2)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.05
  el.appendChild(renderer.domElement)

 
  const pmrem = new THREE.PMREMGenerator(renderer)
  scene.environment = pmrem.fromScene(new RoomEnvironment(), 0.04).texture
  pmrem.dispose()


  const key = new THREE.DirectionalLight(0xffffff, 2.2)
  key.position.set(3, 5, 4)
  scene.add(key)
  const fill = new THREE.DirectionalLight(0xffffff, 0.8)
  fill.position.set(-4, 1, 3)
  scene.add(fill)
  const rimPink = new THREE.DirectionalLight(0x3f8f2e, 2.4)
  rimPink.position.set(-5, 2, -5)
  scene.add(rimPink)
  const rimOrange = new THREE.DirectionalLight(0x8fce4a, 1.8)
  rimOrange.position.set(5, -1, -5)
  scene.add(rimOrange)
  scene.add(new THREE.AmbientLight(0xffffff, 0.35))

  group = new THREE.Group()
  scene.add(group)

  const loader = new GLTFLoader()
  loader.load(
    brainUrl,
    (gltf) => {
      const brain = gltf.scene
      const box = new THREE.Box3().setFromObject(brain)
      const size = box.getSize(new THREE.Vector3())
      const center = box.getCenter(new THREE.Vector3())
      brain.position.sub(center)
      const maxDim = Math.max(size.x, size.y, size.z) || 1
      brain.scale.setScalar(3.4 / maxDim)


      const tint = new THREE.Color(0x9fc36a)
      brain.traverse((o) => {
        const mesh = o as THREE.Mesh
        if (mesh.isMesh) {
          disposables.push(mesh.geometry)
          const mats = Array.isArray(mesh.material) ? mesh.material : [mesh.material]
          mats.forEach((m) => {
            if (!m) return
            disposables.push(m)
            const mat = m as THREE.MeshStandardMaterial
            if (mat.color) mat.color.multiply(tint)
          })
        }
      })

      group.add(brain)
      loaded.value = true
    },
    undefined,
    (err) => console.error('Brain model failed to load', err),
  )

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableZoom = false
  controls.enablePan = false
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.rotateSpeed = 0.55
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.9
  controls.minPolarAngle = Math.PI * 0.28
  controls.maxPolarAngle = Math.PI * 0.72

  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduce) {
    controls.autoRotate = false
    entrance = 1
    entranceArmed = true
  }

  ro = new ResizeObserver(() => {
    const W = el.clientWidth || 1
    const H = el.clientHeight || 1
    camera.aspect = W / H
    camera.updateProjectionMatrix()
    renderer.setSize(W, H)
  })
  ro.observe(el)

  io = new IntersectionObserver(
    (entries) => {
      const vis = entries[0]?.isIntersecting ?? false
      if (vis) {
        entranceArmed = true
        start()
      } else {
        stop()
      }
    },
    { threshold: 0.12 },
  )
  io.observe(el)

  el.addEventListener('pointermove', onPointer)
})

onBeforeUnmount(() => {
  stop()
  io?.disconnect()
  ro?.disconnect()
  container.value?.removeEventListener('pointermove', onPointer)
  controls?.dispose()
  disposables.forEach((d) => d.dispose())
  scene?.environment?.dispose()
  renderer?.dispose()
  renderer?.domElement?.remove()
})
</script>

<template>
  <div ref="container" class="absolute inset-0 transition-opacity duration-1000" :class="loaded ? 'opacity-100' : 'opacity-0'"></div>
</template>
