<script setup lang="ts">
import { useHome } from '../composables/useHome'

import Navbar from '../components/layout/Navbar.vue'
import HeroSection from '../components/hero/HeroSection.vue'
//import BrandsSlider from '../components/brands/BrandsSlider.vue'
import Manifesto from '../components/manifesto/Manifesto.vue'
import FloatingCollage from '../components/collage/FloatingCollage.vue'
import FeatureSection from '../components/features/FeatureSection.vue'
import StudioSection from '../components/studio/StudioSection.vue'
import TeamSection from '../components/teams/TeamSection.vue'
import UpdatesSection from '../components/updates/UpdatesSection.vue'
// import Testimonial from '../components/testimonial/Testimonial.vue'
import CallToAction from '../components/ui/CallToAction.vue'
import Footer from '../components/layout/Footer.vue'

const { content } = useHome()
</script>

<template>
  <div v-if="content">
    <Navbar :brand="content.nav.brand" :links="content.nav.links" :cta="content.nav.cta" />

    <main>
      <!-- Accroche -->
      <HeroSection :hero="content.hero" />

      <!-- <BrandsSlider :brands="content.brands" /> -->

      <!-- Pourquoi : affirmation forte -->
      <Manifesto :text="content.manifesto" />

      <!-- Vue d'ensemble : le processus d'avant-vente -->
      <FloatingCollage
        :title="content.collage.title"
        :cards="content.collage.cards"
        :comment-avatar="content.testimonial.avatar"
      />

      <!-- Produit en profondeur (steps) -->
      <FeatureSection v-if="content.featureBlocks[0]" :block="content.featureBlocks[0]" />

      <!-- Pour qui -->
      <TeamSection :teams="content.teams" />

      <!-- Produit en profondeur (steps) -->
      <StudioSection :studio="content.studio" />

      <!-- Ressources -->
      <UpdatesSection :updates="content.updates" />

      <!-- Preuve, juste avant la conversion -->
      <!-- <Testimonial :testimonial="content.testimonial" /> -->

      <CallToAction :cta="content.cta" />
    </main>

    <Footer :brand="content.nav.brand" :footer="content.footer" />
  </div>
</template>
