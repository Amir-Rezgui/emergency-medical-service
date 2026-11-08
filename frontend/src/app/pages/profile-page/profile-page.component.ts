import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProfileService } from '../../../services/profile.service';
import { UserProfile } from '../../models/profile.model';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css',
})
export class ProfilePageComponent implements OnInit {
  private readonly profileService = inject(ProfileService);

  loading = true;
  profile: UserProfile | null = null;

  async ngOnInit() {
    this.profile = await this.profileService.getUserProfile();
    this.loading = false;
  }

  initials(name: string) {
    return name.split(' ').map((part) => part[0]).slice(0, 2).join('');
  }
}


