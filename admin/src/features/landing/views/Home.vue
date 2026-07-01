<script setup lang="ts">
import { useHome } from '../composables/useHome'

import Navbar from '../components/layout/Navbar.vue'
import HeroSection from '../components/hero/HeroSection.vue'
//import BrandsSlider from '../components/brands/BrandsSlider.vue'
import FloatingCollage from '../components/collage/FloatingCollage.vue'
import Manifesto from '../components/manifesto/Manifesto.vue'
import FeatureSection from '../components/features/FeatureSection.vue'
import StudioSection from '../components/studio/StudioSection.vue'
import TeamSection from '../components/teams/TeamSection.vue'
import Testimonial from '../components/testimonial/Testimonial.vue'
import UpdatesSection from '../components/updates/UpdatesSection.vue'
import CallToAction from '../components/ui/CallToAction.vue'
import Footer from '../components/layout/Footer.vue'

const { content } = useHome()
</script>

<template>
  <div v-if="content">
    <Navbar :brand="content.nav.brand" :links="content.nav.links" :cta="content.nav.cta" />

    <main>
      <HeroSection :hero="content.hero" />

      <!-- <BrandsSlider :brands="content.brands" /> -->

      <FloatingCollage
        :title="content.collage.title"
        :cards="content.collage.cards"
        :comment-avatar="content.testimonial.avatar"
      />

      <Manifesto :text="content.manifesto" />

      <FeatureSection v-if="content.featureBlocks[0]" :block="content.featureBlocks[0]" />

      <StudioSection :studio="content.studio" />

      <TeamSection :teams="content.teams" />

      <Testimonial :testimonial="content.testimonial" />

      <UpdatesSection :updates="content.updates" />

      <CallToAction :cta="content.cta" />
    </main>

    <Footer :brand="content.nav.brand" :footer="content.footer" />
  </div>
</template>
